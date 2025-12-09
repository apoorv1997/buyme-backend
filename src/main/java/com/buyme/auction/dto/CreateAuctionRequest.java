package com.buyme.auction.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CreateAuctionRequest(
        String title,
        String description,
        Long categoryId,
        BigDecimal startPrice,
        BigDecimal bidIncrement,
        BigDecimal reservePrice,
        Instant startTime,
        Instant endTime,
        boolean featured
) {
}
