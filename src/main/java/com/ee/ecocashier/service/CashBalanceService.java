package com.ee.ecocashier.service;

import com.ee.ecocashier.model.CashBalanceResponse;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public interface CashBalanceService {

    CashBalanceResponse getBalance(String cashierId, OffsetDateTime dateFrom, OffsetDateTime dateTo, ZoneId clientZone);
}
