package com.ee.ecocashier.service;

import com.ee.ecocashier.model.CashBalance;
import com.ee.ecocashier.model.CashOperationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
            Map<String, CashBalance> balances = loadBalancesFromFile();

            String key = request.cashierId() + "_" + request.currency();
            CashBalance balance = balances.getOrDefault(
                    key,
                    new CashBalance(request.currency().name(), BigDecimal.ZERO, new HashMap<>())
            );

            //handling denominations
            for (Map.Entry<Integer, Integer> entry : request.denominations().entrySet()) {
                balance.denominations().merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
            BigDecimal newTotal = balance.denominations().entrySet().stream()
                    .map(e -> BigDecimal.valueOf(e.getKey()).multiply(BigDecimal.valueOf(e.getValue())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            balances.put(key, new CashBalance(balance.currency(), newTotal, balance.denominations()));

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

    private Map<String, CashBalance> loadBalancesFromFile() {
        Map<String, CashBalance> balances = new HashMap<>();

        Path filePath = Paths.get("balances_" + LocalDate.now(ZoneOffset.UTC) + ".csv");

        if (Files.exists(filePath)) {
            try {
                List<String> lines = Files.readAllLines(filePath);
                for (String line : lines) {
                    if (line.startsWith("cashier")) continue;
                    String[] parts = line.split(",", 4);
                    if (parts.length != 4) continue;

                    String cashier = parts[0];
                    String currency = parts[1];
                    BigDecimal total = new BigDecimal(parts[2]);

                    //parse denominations
                    Map<Integer, Integer> denomMap = new HashMap<>();
                    for (String entry : parts[3].split(";")) {
                        if (entry.isEmpty()) continue;
                        String[] kv = entry.split("=");
                        denomMap.put(Integer.parseInt(kv[0]), Integer.parseInt(kv[1]));
                    }

                    String key = cashier + "_" + currency;
                    balances.put(key, new CashBalance(currency, total, denomMap));
                }
            } catch (IOException | NumberFormatException e) {
                throw new RuntimeException("Failed to read balances file", e);
            }
        }

        return balances;
    }

    private void saveBalancesToFile(Map<String, CashBalance> balances) {
        try {
            Path filePath = Paths.get("balances_" + LocalDate.now(ZoneOffset.UTC) + ".csv");

            StringBuilder sb = new StringBuilder();
            sb.append("cashier,currency,balance,denominations").append(System.lineSeparator());

            for (Map.Entry<String, CashBalance> entry : balances.entrySet()) {
                String[] keyParts = entry.getKey().split("_");
                String cashier = keyParts[0];
                CashBalance cb = entry.getValue();

                String denomStr = cb.denominations().entrySet().stream()
                        .map(e -> e.getKey() + "=" + e.getValue())
                        .collect(Collectors.joining(";"));

                sb.append(cashier).append(",")
                        .append(cb.currency()).append(",")
                        .append(cb.total()).append(",")
                        .append(denomStr)
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
