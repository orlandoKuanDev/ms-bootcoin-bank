package com.example.msbootcoin.infrastructure.repository;

import com.example.msbootcoin.domain.entity.BootCoin;
import com.example.msbootcoin.infrastructure.common.IRepository;
import reactor.core.publisher.Mono;

public interface IBootCoinRepository extends IRepository<BootCoin, String> {
    Mono<BootCoin> findBootCoinByCustomer_Phone(String phone);
}
