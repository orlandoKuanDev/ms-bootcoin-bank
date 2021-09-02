package com.example.msbootcoin.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TransferencePayDTO {
    private String accountSeller;
    private String accountBuyer;
    private String verificationCode;
}
