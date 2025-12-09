package com.buyme.auction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AuctionResponse(
        Long id,
        String title,
        String description,
        Long sellerId,
        String sellerName,
        Long categoryId,
        String categoryName,
        BigDecimal startPrice,
        BigDecimal currentPrice,
        BigDecimal bidIncrement,
        Instant startTime,
        Instant endTime,
        String status,
        boolean featured,
        Long winnerId,
        BigDecimal finalPrice
) {
}
