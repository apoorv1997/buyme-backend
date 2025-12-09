package com.buyme.alert.dto;

import java.math.BigDecimal;

public record AlertCreateRequest(
        String keywords,
        Long categoryId,
        BigDecimal minPrice,
        BigDecimal maxPrice
) {
}
