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
@Document(collection = "BootCoin")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BootCoin {
    @Id
    private String id;

    @Field(name = "customer")
    private Customer customer;

    @Field(name = "amountCoin")
    private Integer amountCoin;

    @Field(name = "openBootCoinDate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openBCoinDate = LocalDateTime.now();
}
