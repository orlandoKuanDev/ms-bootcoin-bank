package com.example.msbootcoin.infrastructure.repository;

import com.example.msbootcoin.domain.entity.BootCoin;
import com.example.msbootcoin.infrastructure.common.BaseService;
import com.example.msbootcoin.infrastructure.common.IRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BootCoinService extends BaseService<BootCoin, String> implements IBootCoinService {
    private final IBootCoinRepository bootCoinRepository;

    @Autowired
    public BootCoinService(IBootCoinRepository bootCoinRepository) {
        this.bootCoinRepository = bootCoinRepository;
    }

    @Override
    protected IRepository<BootCoin, String> getRepository() {
        return bootCoinRepository;
    }
}
