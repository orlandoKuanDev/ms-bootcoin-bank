package com.example.msbootcoin.domain.service;

import com.example.msbootcoin.domain.entity.Transference;
import com.example.msbootcoin.infrastructure.common.BaseService;
import com.example.msbootcoin.infrastructure.common.IRepository;
import com.example.msbootcoin.infrastructure.repository.ITransferenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class TransferenceService extends BaseService<Transference, String> implements ITransferenceService{

    private final ITransferenceRepository repository;

    @Autowired
    public TransferenceService(ITransferenceRepository repository) {
        this.repository = repository;
    }

    @Override
    protected IRepository<Transference, String> getRepository() {
        return repository;
    }

    @Override
    public Mono<Transference> findByVerificationCode(String code) {
        return repository.findByVerificationCode(code);
    }
}
