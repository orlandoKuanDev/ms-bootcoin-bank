package com.example.msbootcoin.infrastructure.repository;

import com.example.msbootcoin.domain.entity.Transference;
import com.example.msbootcoin.infrastructure.common.IRepository;
import reactor.core.publisher.Mono;

public interface ITransferenceRepository extends IRepository<Transference, String> {
    Mono<Transference> findByVerificationCode(String code);
}
