package com.ee.ecocashier.controller;

import com.ee.ecocashier.model.CashOperationRequest;
import com.ee.ecocashier.service.CashOperationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CashOperationController {

    private final CashOperationService cashOperationService;

    @PostMapping("/cash-operation")
    public ResponseEntity<String> handleCashOperation(@Valid @RequestBody CashOperationRequest request) {

        cashOperationService.handleOperation(request);

        return ResponseEntity.ok("Operation successful");
    }
}
