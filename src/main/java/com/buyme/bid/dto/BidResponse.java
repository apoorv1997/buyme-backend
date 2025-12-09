package com.buyme.bid.dto;

import java.math.BigDecimal;

public record BidResponse(
        Long id,
        Long auctionId,
        Long bidderId,
        String bidderName,
        BigDecimal amount,
        BigDecimal maxAutoBidAmount,
        boolean winningAtPlacement
) {
}
