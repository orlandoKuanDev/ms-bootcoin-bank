package com.example.msbootcoin.domain.service;

import com.example.msbootcoin.domain.entity.Transference;
import com.example.msbootcoin.infrastructure.common.IBaseService;
import reactor.core.publisher.Mono;

public interface ITransferenceService extends IBaseService<Transference, String> {
    Mono<Transference> findByVerificationCode(String code);
}