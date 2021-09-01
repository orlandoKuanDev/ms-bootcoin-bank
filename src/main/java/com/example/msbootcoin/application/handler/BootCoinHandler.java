package com.example.msbootcoin.application.handler;

import com.example.msbootcoin.domain.entity.BootCoin;
import com.example.msbootcoin.infrastructure.repository.IBootCoinService;
import com.example.msbootcoin.infrastructure.topic.producer.BootCoinProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class BootCoinHandler {
    private final IBootCoinService bootCoinService;
    private final BootCoinProducer bootCoinProducer;

    @Autowired
    public BootCoinHandler(IBootCoinService bootCoinService, BootCoinProducer bootCoinProducer) {
        this.bootCoinService = bootCoinService;
        this.bootCoinProducer = bootCoinProducer;
    }

    public Mono<ServerResponse> findAll(ServerRequest request) {
        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(bootCoinService.findAll(), BootCoin.class);
    }

}
