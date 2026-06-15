package com.omarmujcic.timetracking.core.tickettrackz.mapper;

import java.math.BigDecimal;
import java.math.RoundingMode;

final class TicketMapperSupport {

    private TicketMapperSupport() {
    }

    static String trimRequired(String value) {
        return value == null ? "" : value.trim();
    }

    static String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    static BigDecimal scaleHours(BigDecimal value) {
        return value == null ? null : value.setScale(2, RoundingMode.HALF_UP);
    }
}
