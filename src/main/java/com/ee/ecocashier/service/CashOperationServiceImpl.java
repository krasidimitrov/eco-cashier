package com.ee.ecocashier.service;

import com.ee.ecocashier.model.CashOperationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
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
            Map<String, BigDecimal> balances = loadBalancesFromFile();

            String key = request.cashierId() + "_" + request.currency();
            BigDecimal current = balances.getOrDefault(key, BigDecimal.ZERO);

            BigDecimal newBalance = switch (request.operationType()) {
                case DEPOSIT -> current.add(request.amount());
                case WITHDRAW -> {
                    if (current.compareTo(request.amount()) < 0) {
                        throw new IllegalArgumentException("Insufficient funds for withdrawal");
                    }
                    yield current.subtract(request.amount());
                }
            };

            balances.put(key, newBalance);

            saveBalancesToFile(balances);
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

        String timestampUtc = LocalDate.now(ZoneOffset.UTC).toString();
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

    private Map<String, BigDecimal> loadBalancesFromFile() {
        Map<String, BigDecimal> balances = new HashMap<>();

        Path filePath = Paths.get("balances_" + LocalDate.now(ZoneOffset.UTC) + ".csv");

        if (Files.exists(filePath)) {
            try {
                List<String> lines = Files.readAllLines(filePath);
                for (String line : lines) {
                    if (line.startsWith("cashier")) continue;
                    String[] parts = line.split(",");
                    if (parts.length != 3) continue;
                    String key = parts[0] + "_" + parts[1];
                    BigDecimal balance = new BigDecimal(parts[2]);
                    balances.put(key, balance);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to read balances file", e);
            }
        }

        return balances;
    }

    private void saveBalancesToFile(Map<String, BigDecimal> balances) {
        try {
            Path filePath = Paths.get("balances_" + LocalDate.now(ZoneOffset.UTC) + ".csv");

            StringBuilder sb = new StringBuilder();
            sb.append("cashier,currency,balance").append(System.lineSeparator());

            for (Map.Entry<String, BigDecimal> entry : balances.entrySet()) {
                String[] parts = entry.getKey().split("_");
                sb.append(parts[0]).append(",")
                        .append(parts[1]).append(",")
                        .append(entry.getValue())
                        .append(System.lineSeparator());
            }

            Files.writeString(filePath, sb.toString(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

        } catch (IOException e) {
            throw new RuntimeException("Failed to save balances file", e);
        }
    }

    private void initCsvFile() throws IOException {
        if (!Files.exists(Path.of(csvFilePathAsString))) {
            String header = "timestamp,cashier,operation,currency,amount,denominations";
            Files.writeString(Path.of(csvFilePathAsString), header + System.lineSeparator(),
                    StandardOpenOption.CREATE);
        }
    }
}
