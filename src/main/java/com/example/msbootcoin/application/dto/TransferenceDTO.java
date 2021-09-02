package com.example.msbootcoin.application.dto;

import com.example.msbootcoin.domain.entity.BootCoin;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransferenceDTO {
    private BootCoin buyer;
    private BootCoin seller;
    private Double amount;
    private Integer amountCoin;
    private String statusTransaction;
    private String verificationCode;
}