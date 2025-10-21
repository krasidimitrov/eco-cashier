package com.ee.ecocashier.service;

import com.ee.ecocashier.model.CashOperationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CashOperationServiceImpl implements CashOperationService {

    @Value("${cash.transaction.file}")
    private String csvFilePathAsString;

    @Override
    public void handleOperation(CashOperationRequest request) {
        switch (request.operationType()) {
            case DEPOSIT -> deposit(request);
            case WITHDRAW -> withdraw(request);
        }
    }

    private void deposit(CashOperationRequest request) {
        try {
            storeTransaction(request.cashierId(), request.operationType().name(), request.currency().name(), request.amount(), request.denominations());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void withdraw(CashOperationRequest request) {
        try {
            storeTransaction(request.cashierId(), request.operationType().name(), request.currency().name(), request.amount(), request.denominations());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String serializeDenominations(Map<Integer, Integer> denominations) {
        return denominations.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
    }


    private void storeTransaction(String cashierId,
                                 String operationType,
                                 String currency,
                                 BigDecimal amount,
                                 Map<Integer, Integer> denominations) throws IOException {

        //Initializing the transaction history file if it does not exist
        initCsvFile();

        String timestampUtc = DateTimeFormatter.ISO_INSTANT.format(Instant.now());
        String line = String.join(",",
                timestampUtc,
                cashierId,
                operationType,
                currency,
                amount.toPlainString(),
                serializeDenominations(denominations)
        );

        Files.writeString(Path.of(csvFilePathAsString), line + System.lineSeparator(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }

    private void initCsvFile() throws IOException {
        if (!Files.exists(Path.of(csvFilePathAsString))) {
            String header = "timestamp,cashier,operation,currency,amount,denominations";
            Files.writeString(Path.of(csvFilePathAsString), header + System.lineSeparator(),
                    StandardOpenOption.CREATE);
        }
    }
}
