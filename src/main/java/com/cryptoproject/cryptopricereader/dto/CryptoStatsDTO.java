package com.cryptoproject.cryptopricereader.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CryptoStatsDTO {
    private String symbol;
    private Double volatilityIndex;
}
