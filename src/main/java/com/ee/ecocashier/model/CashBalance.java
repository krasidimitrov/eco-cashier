package com.ee.ecocashier.model;

import java.math.BigDecimal;
import java.util.Map;

public record CashBalance(
        String currency,
        BigDecimal total,
        Map<Integer, Integer> denominations
) {}