package com.example.msbootcoin.domain.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "exchange")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Exchange {
    @Id
    private String id;

    @Field(name = "provider")
    private String name;

    @Field(name = "amountCoin")
    private Integer amountCoin;
}
