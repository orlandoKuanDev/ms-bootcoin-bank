package com.example.msbootcoin.application.handler;

import com.example.msbootcoin.application.dto.*;
import com.example.msbootcoin.application.mapper.TransferenceConverter;
import com.example.msbootcoin.application.web.AcquisitionService;
import com.example.msbootcoin.domain.entity.*;
import com.example.msbootcoin.domain.service.ITransferenceService;
import com.example.msbootcoin.infrastructure.repository.IBootCoinService;
import com.example.msbootcoin.infrastructure.topic.producer.BootCoinProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class BootCoinHandler {
    private final IBootCoinService bootCoinService;
    private final BootCoinProducer bootCoinProducer;
    private final AcquisitionService acquisitionService;
    private final TransferenceConverter transferenceConverter;
    private final ITransferenceService transferenceService;
    @Autowired
    public BootCoinHandler(IBootCoinService bootCoinService, BootCoinProducer bootCoinProducer, AcquisitionService acquisitionService, TransferenceConverter transferenceConverter, ITransferenceService transferenceService) {
        this.bootCoinService = bootCoinService;
        this.bootCoinProducer = bootCoinProducer;
        this.acquisitionService = acquisitionService;
        this.transferenceConverter = transferenceConverter;
        this.transferenceService = transferenceService;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(bootCoinService.findAll(), BootCoin.class);
    }
    public Mono<ServerResponse> findByVerificationCode(ServerRequest request){
        String code = request.pathVariable("code");
        return transferenceService.findByVerificationCode(code)
                .flatMap(p -> ServerResponse.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(p))
                .switchIfEmpty(Mono.error(new RuntimeException("THE BOOTCOIN DOES NOT EXIST")));
    }

    public Mono<Customer> createCustomer(Mono<BootCoinCreateRequestDTO> walletRequest){
        Mono<Customer> customerForConsumer = Mono.just(new Customer());
        return walletRequest
                .zipWith(customerForConsumer, (req, customer) -> {
                    customer.setCustomerType("PERSONAL");
                    customer.setCustomerIdentityType(req.getCustomerIdentityType());
                    customer.setCustomerIdentityNumber(req.getCustomerIdentityNumber());
                    customer.setName(req.getName());
                    customer.setEmail(req.getEmail());
                    customer.setPhone(req.getPhone());
                    customer.setAddress(req.getAddress());
                    bootCoinProducer.sendSaveCustomerService(customer);
                    return customer;
                });
    }
    public Mono<ServerResponse> createBootCoin(ServerRequest request){
        Mono<BootCoinCreateRequestDTO> walletRequest = request.bodyToMono(BootCoinCreateRequestDTO.class);
        return walletRequest
                .as(this::createCustomer)
                .checkpoint("after customer create consumer")
                .delayElement(Duration.ofMillis(2000))
                .zipWhen(customer -> {
                    Acquisition createAcquisitionDTO = new Acquisition();
                    List<Customer> customers = new ArrayList<>();
                    customers.add(customer);
                    createAcquisitionDTO.setCustomerHolder(customers);
                    createAcquisitionDTO.setProduct(Product.builder()
                            .productName("BOOTCOIN").build());
                    createAcquisitionDTO.setInitial(0.0);
                    bootCoinProducer.sendSaveAcquisitionService(createAcquisitionDTO);
                    return Mono.just(createAcquisitionDTO);
                })
                .flatMap(response -> {
                    BootCoin bootCoin = new BootCoin();
                    bootCoin.setCustomer(response.getT1());
                    bootCoin.setAmountCoin(0);
                    return bootCoinService.create(bootCoin);
                })
                .flatMap(bootCoin -> ServerResponse.created(URI.create("/bootCoin/".concat(bootCoin.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(bootCoin))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }

    public Mono<ServerResponse> transferenceBootCoinPending(ServerRequest request){
        Mono<TransferenceRequestDTO> transferRequest = request.bodyToMono(TransferenceRequestDTO.class);
        return transferRequest
                .zipWhen(transfer -> {
                    Mono<BootCoin> origenBootCoin= bootCoinService
                            .findBootCoinByCustomer_Phone(transfer.getPhoneOrigen())
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin origen does not exist")));

                    Mono<BootCoin> destineBootCoin = bootCoinService
                            .findBootCoinByCustomer_Phone(transfer.getPhoneDestine())
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin destine does not exist")));
                    return Mono.zip(origenBootCoin, destineBootCoin);
                })
                .flatMap(result -> {

            Double amount = result.getT1().getAmount();
            Integer coins = (int) Math.round(amount / 3.5);
            DetailsDTO detailsDTO = new DetailsDTO();
            detailsDTO.setAmount(amount);
            detailsDTO.setAmountCoin(coins);

            BootCoin seller = result.getT2().getT1();
            seller.setAmountCoin(seller.getAmountCoin() + coins);
            BootCoin buyer = result.getT2().getT2();
            buyer.setAmountCoin(seller.getAmountCoin() - coins);

            Mono<BootCoin> sellerBootCoin = Mono.just(seller);
            Mono<BootCoin> buyerBootCoin = Mono.just(buyer);
            Mono<DetailsDTO> report = Mono.just(detailsDTO);
            return Mono.zip(sellerBootCoin, buyerBootCoin, report);
        })
                .flatMap(transference -> {
                    TransferenceDTO transferenceDTO = new TransferenceDTO();
                    transferenceDTO.setSeller(transference.getT1());
                    transferenceDTO.setBuyer(transference.getT2());
                    transferenceDTO.setAmount(transference.getT3().getAmount());
                    transferenceDTO.setAmountCoin(transference.getT3().getAmountCoin());
                    transferenceDTO.setStatusTransaction("PENDING");
                    transferenceDTO.setTransactionCode("A7SD4A6D");
                    Transference transferenceCreate = transferenceConverter.convertToEntity(transferenceDTO, new Transference());
                    return transferenceService.create(transferenceCreate);
                })
                .log()
                .flatMap(transference -> ServerResponse.created(URI.create("/transference/".concat(transference.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(transference))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }
    public Mono<ServerResponse> transferenceApproved(ServerRequest request){
        String code = request.pathVariable("code");
        Mono<Transference> transferenceMono = transferenceService.findByVerificationCode(code);
        return transferenceMono
                .zipWhen(dataWallet -> {
                    log.info("origenBootCoin, {}", (dataWallet.getBuyer().getCustomer().getCustomerIdentityNumber()));
                    log.info("destineBootCoin, {}", (dataWallet.getSeller().getCustomer().getCustomerIdentityNumber()));
                    return acquisitionService
                            .findAllByCustomer(dataWallet.getSeller().getCustomer().getCustomerIdentityNumber())
                            .collectList()
                            .flatMap(acquisitions -> {
                                log.info("ACQUISITION_LIST, {}", acquisitions);
                                Acquisition origen = acquisitions.stream()
                                        .filter(acquisition -> acquisition.getProduct().getProductName().equals("BOOTCOIN"))
                                        .findFirst()
                                        .orElse(new Acquisition());
                                CreateTransferenceDTO retire = new CreateTransferenceDTO();
                                retire.setAmount(dataWallet.getAmount());
                                retire.setAccountNumber(origen.getBill().getAccountNumber());
                                retire.setDescription(String.format("send money from %s to %s",
                                        dataWallet.getSeller().getCustomer().getPhone(),
                                        dataWallet.getBuyer().getCustomer().getPhone()));
                                retire.setCardNumber("");
                                log.info("RETIRE, {}", retire);
                                bootCoinProducer.sendSaveRetireService(retire);
                                return Mono.just(origen);
                            });
                })
                .zipWhen(dataWalletDestine -> {
                    return acquisitionService
                            .findAllByCustomer(dataWalletDestine.getT1().getBuyer().getCustomer().getCustomerIdentityNumber())
                            .collectList()
                            .flatMap(acquisitionsDestine -> {
                                Acquisition destine = acquisitionsDestine.stream()
                                        .filter(acquisition -> acquisition.getProduct().getProductName().equals("BOOTCOIN"))
                                        .findFirst()
                                        .orElse(new Acquisition());
                                CreateTransferenceDTO deposit = new CreateTransferenceDTO();
                                deposit.setAmount(dataWalletDestine.getT1().getAmount());
                                deposit.setAccountNumber(destine.getBill().getAccountNumber());
                                deposit.setDescription(String.format("receive money from %s to %s",
                                        dataWalletDestine.getT1().getBuyer().getCustomer().getPhone(),
                                        dataWalletDestine.getT1().getSeller().getCustomer().getPhone()));
                                deposit.setCardNumber("");
                                bootCoinProducer.sendSaveDepositService(deposit);
                                return Mono.just(deposit);
                            });
                })
                .flatMap(result -> {
                    //con la cantidad calcular las gemas
                    Double amount = result.getT1().getT1().getAmount();
                    Integer coins = (int) Math.round(amount / 3.5);
                    DetailsDTO detailsDTO = new DetailsDTO();
                    detailsDTO.setAmount(amount);
                    detailsDTO.setAmountCoin(coins);

                    BootCoin seller = result.getT1().getT1().getSeller();
                    //vendedor sumo coins
                    seller.setAmountCoin(seller.getAmountCoin() + coins);
                    //comprador resto coins
                    BootCoin buyer = result.getT1().getT1().getBuyer();
                    buyer.setAmountCoin(seller.getAmountCoin() - coins);
                    Mono<BootCoin> sellerBootCoin= bootCoinService
                            .update(seller)
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin seller does not exist")));
                    Mono<BootCoin> buyerBootCoin= bootCoinService
                            .update(buyer)
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin buyer does not exist")));
                    Mono<DetailsDTO> report = Mono.just(detailsDTO);
                    return Mono.zip(sellerBootCoin, buyerBootCoin, report);
                })
                .flatMap(transference -> {
                    TransferenceDTO transferenceDTO = new TransferenceDTO();
                    transferenceDTO.setSeller(transference.getT1());
                    transferenceDTO.setBuyer(transference.getT2());
                    transferenceDTO.setAmount(transference.getT3().getAmount());
                    transferenceDTO.setAmountCoin(transference.getT3().getAmountCoin());
                    Transference transferenceCreate = transferenceConverter.convertToEntity(transferenceDTO, new Transference());
                    return transferenceService.create(transferenceCreate);
                })
                .log()
                .flatMap(transference -> ServerResponse.created(URI.create("/transference/".concat(transference.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(transference))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }

    public Mono<ServerResponse> transferenceBootCoinApproved(ServerRequest request){
        Mono<TransferenceRequestDTO> transferRequest = request.bodyToMono(TransferenceRequestDTO.class);
        return transferRequest
                .zipWhen(transfer -> {
                    Mono<BootCoin> origenBootCoin= bootCoinService
                            .findBootCoinByCustomer_Phone(transfer.getPhoneOrigen())
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin origen does not exist")));

                    Mono<BootCoin> destineBootCoin = bootCoinService
                            .findBootCoinByCustomer_Phone(transfer.getPhoneDestine())
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin destine does not exist")));
                    return Mono.zip(origenBootCoin, destineBootCoin);
                })
                .zipWhen(dataWallet -> {
                    log.info("origenBootCoin, {}", (dataWallet.getT2().getT1().getCustomer().getCustomerIdentityNumber()));
                    log.info("destineBootCoin, {}", (dataWallet.getT2().getT2().getCustomer().getCustomerIdentityNumber()));
                    return acquisitionService
                            .findAllByCustomer(dataWallet.getT2().getT1().getCustomer().getCustomerIdentityNumber())
                            .collectList()
                            .flatMap(acquisitions -> {
                                log.info("ACQUISITION_LIST, {}", acquisitions);
                                Acquisition origen = acquisitions.stream()
                                        .filter(acquisition -> acquisition.getProduct().getProductName().equals("BOOTCOIN"))
                                        .findFirst()
                                        .orElse(new Acquisition());
                                CreateTransferenceDTO retire = new CreateTransferenceDTO();
                                retire.setAmount(dataWallet.getT1().getAmount());
                                retire.setAccountNumber(origen.getBill().getAccountNumber());
                                retire.setDescription(String.format("send money from %s to %s",
                                        dataWallet.getT2().getT1().getCustomer().getPhone(),
                                        dataWallet.getT2().getT2().getCustomer().getPhone()));
                                retire.setCardNumber("");
                                log.info("RETIRE, {}", retire);
                                bootCoinProducer.sendSaveRetireService(retire);
                                return Mono.just(origen);
                            });
                })
                .zipWhen(dataWalletDestine -> {
                    return acquisitionService
                            .findAllByCustomer(dataWalletDestine.getT1().getT2().getT2().getCustomer().getCustomerIdentityNumber())
                            .collectList()
                            .flatMap(acquisitionsDestine -> {
                                Acquisition destine = acquisitionsDestine.stream()
                                        .filter(acquisition -> acquisition.getProduct().getProductName().equals("BOOTCOIN"))
                                        .findFirst()
                                        .orElse(new Acquisition());
                                CreateTransferenceDTO deposit = new CreateTransferenceDTO();
                                deposit.setAmount(dataWalletDestine.getT1().getT1().getAmount());
                                deposit.setAccountNumber(destine.getBill().getAccountNumber());
                                deposit.setDescription(String.format("receive money from %s to %s",
                                        dataWalletDestine.getT1().getT2().getT1().getCustomer().getPhone(),
                                        dataWalletDestine.getT1().getT2().getT2().getCustomer().getPhone()));
                                deposit.setCardNumber("");
                                bootCoinProducer.sendSaveDepositService(deposit);
                                return Mono.just(deposit);
                            });
                })
                .flatMap(result -> {
                    //con la cantidad calcular las gemas
                    Double amount = result.getT1().getT1().getT1().getAmount();
                    Integer coins = (int) Math.round(amount / 3.5);
                    DetailsDTO detailsDTO = new DetailsDTO();
                    detailsDTO.setAmount(amount);
                    detailsDTO.setAmountCoin(coins);

                    BootCoin seller = result.getT1().getT1().getT2().getT1();
                    //vendedor sumo coins
                    seller.setAmountCoin(seller.getAmountCoin() + coins);
                    //comprador resto coins
                    BootCoin buyer = result.getT1().getT1().getT2().getT2();
                    buyer.setAmountCoin(seller.getAmountCoin() - coins);
                    Mono<BootCoin> sellerBootCoin= bootCoinService
                            .update(seller)
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin seller does not exist")));
                    Mono<BootCoin> buyerBootCoin= bootCoinService
                            .update(buyer)
                            .switchIfEmpty(Mono.error(new RuntimeException("BootCoin buyer does not exist")));
                    Mono<DetailsDTO> report = Mono.just(detailsDTO);
                   return Mono.zip(sellerBootCoin, buyerBootCoin, report);
                })
                .flatMap(transference -> {
                    TransferenceDTO transferenceDTO = new TransferenceDTO();
                    transferenceDTO.setSeller(transference.getT1());
                    transferenceDTO.setBuyer(transference.getT2());
                    transferenceDTO.setAmount(transference.getT3().getAmount());
                    transferenceDTO.setAmountCoin(transference.getT3().getAmountCoin());
                    Transference transferenceCreate = transferenceConverter.convertToEntity(transferenceDTO, new Transference());
                    return transferenceService.create(transferenceCreate);
                })
                .log()
                .flatMap(transference -> ServerResponse.created(URI.create("/transference/".concat(transference.getId())))
                        .contentType(APPLICATION_JSON)
                        .bodyValue(transference))
                .onErrorResume(error -> Mono.error(new RuntimeException(error.getMessage())));
    }

}
