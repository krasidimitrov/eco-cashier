package com.ee.ecocashier.service;

import com.ee.ecocashier.model.CashBalanceResponse;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
public class CashBalanceServiceImpl implements CashBalanceService {

    @Override
    public CashBalanceResponse getBalance(String cashierId, OffsetDateTime dateFrom, OffsetDateTime dateTo, ZoneId clientZone) {
        return null;
    }
}
