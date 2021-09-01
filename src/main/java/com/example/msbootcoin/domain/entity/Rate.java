package com.example.msbootcoin.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Document(collection = "rate")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Rate {
    @Id
    private String id;

    @Field(name = "amount")
    private Double purchaseRate;

    @Field(name = "openRate")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime openRate = LocalDateTime.now();
}
