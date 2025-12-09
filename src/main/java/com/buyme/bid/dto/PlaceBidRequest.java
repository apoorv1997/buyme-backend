package com.buyme.bid.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record PlaceBidRequest(
        @NotNull BigDecimal amount,
        BigDecimal maxAutoBidAmount
) {
}
