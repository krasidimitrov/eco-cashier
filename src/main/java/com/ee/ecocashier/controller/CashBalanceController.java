package com.ee.ecocashier.controller;

import com.ee.ecocashier.model.CashBalanceRequest;
import com.ee.ecocashier.model.CashBalanceResponse;
import com.ee.ecocashier.service.CashBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZoneId;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CashBalanceController {

    private final CashBalanceService cashBalanceService;

    @GetMapping("/cash-balance")
    public ResponseEntity<?> getBalance(@ModelAttribute CashBalanceRequest request) {

        ZoneId clientZone = parseZoneIdOrDefault(request.getZoneId());

        log.info("Balance check for cashierId={} from={} to={} zone={}", request.getCashierId(), request.getDateFrom(), request.getDateTo(), clientZone);

        CashBalanceResponse response =
                cashBalanceService.getBalance(request.getCashierId(), request.getDateFrom(), request.getDateTo(), clientZone);

        return ResponseEntity.ok(response);
    }

    private ZoneId parseZoneIdOrDefault(String zone) {
        if (zone == null || zone.isBlank()) {
            return ZoneId.of("UTC");
        }
        try {
            return ZoneId.of(zone);
        } catch (Exception e) {
            log.warn("Invalid timezone header '{}', defaulting to UTC", zone);
            return ZoneId.of("UTC");
        }
    }
}
