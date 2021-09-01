package com.example.msbootcoin.infrastructure.repository;

import com.example.msbootcoin.domain.entity.BootCoin;
import com.example.msbootcoin.infrastructure.common.IBaseService;
import reactor.core.publisher.Mono;

public interface IBootCoinService extends IBaseService<BootCoin, String> {
    Mono<BootCoin> findBootCoinByCustomer_Phone(String phone);

}
