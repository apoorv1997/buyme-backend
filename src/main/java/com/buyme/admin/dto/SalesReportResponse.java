package com.buyme.admin.dto;

import java.math.BigDecimal;
import java.util.Map;

public record SalesReportResponse(
        BigDecimal totalEarnings,
        Map<Long, BigDecimal> earningsPerAuction,
        Map<Long, BigDecimal> earningsPerSeller,
        Map<Long, BigDecimal> earningsPerCategory
) {
}
