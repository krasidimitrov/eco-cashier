package com.ee.ecocashier.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CashBalanceResponse {

    private String cashierId;

    private Map<LocalDate, DailyBalanceSnapshot> dailySnapshots;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ToString
    public static class DailyBalanceSnapshot {

        private Map<String, BigDecimal> totalBalance;
        private Map<String, Map<Integer, Integer>> denominations;
    }
}
