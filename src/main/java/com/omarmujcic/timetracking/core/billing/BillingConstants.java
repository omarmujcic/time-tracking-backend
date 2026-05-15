package com.omarmujcic.timetracking.core.billing;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

final class BillingConstants {

    static final String CURRENCY = "EUR";
    static final String LEGACY_PROJECT_PREFIX = "name:";
    static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    static final BigDecimal ONE = BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
    static final DateTimeFormatter MONTH_LABEL = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private BillingConstants() {
    }
}
