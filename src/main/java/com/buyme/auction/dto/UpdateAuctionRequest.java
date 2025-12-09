package com.buyme.auction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record UpdateAuctionRequest(
        String title,
        String description,
        Long categoryId,
        BigDecimal bidIncrement,
        Instant endTime,
        boolean featured,
        String status
) {
}
