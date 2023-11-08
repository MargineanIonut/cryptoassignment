package com.cryptoproject.cryptopricereader.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CryptoCoin {
    private LocalDateTime timestamp;
    private String symbol;
    private Double price;
    private Double volatilityIndex;
}
