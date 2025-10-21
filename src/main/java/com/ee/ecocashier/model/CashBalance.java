package com.ee.ecocashier.model;

import com.ee.ecocashier.enums.CurrencyType;

import java.math.BigDecimal;
import java.util.Map;

public record CashBalance(
        CurrencyType currency,
        BigDecimal total,
        Map<Integer, Integer> denominations
) {}