package com.example.msbootcoin.domain.entity;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;

public class Transaction {
    @Indexed(unique = true)
    @Field(name = "verificationCode")
    private String verificationCode;
}
