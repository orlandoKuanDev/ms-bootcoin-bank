package com.example.msbootcoin.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "transferenceBootCoin")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transference {
    @Id
    private String id;

    @Field(name = "buyer")
    private BootCoin buyer;

    @Field(name = "seller")
    private BootCoin seller;

    @Field(name = "amount")
    private Double amount;

    @Field(name = "amountCoin")
    private Integer amountCoin;

    @Field(name = "statusTransaction")
    private String statusTransaction;

    @Indexed(unique = true)
    @Field(name = "verificationCode")
    private String verificationCode;

    @Field(name = "transactionDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime transactionDate = LocalDateTime.now();
}
