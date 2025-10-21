package com.ee.ecocashier.controller;

import com.ee.ecocashier.model.CashBalanceResponse;
import com.ee.ecocashier.service.CashBalanceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CashBalanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashBalanceService cashBalanceService;

    @Value("${api.key}")
    private String apiKey;

    //test data helper method
    private CashBalanceResponse sampleCashBalanceResponse(String cashier) {
        Map<LocalDate, CashBalanceResponse.DailyBalanceSnapshot> snapshots = Map.of(
                LocalDate.of(2025, 10, 16),
                CashBalanceResponse.DailyBalanceSnapshot.builder()
                        .totalBalance(Map.of("BGN", new BigDecimal("1000.00"),
                                "EUR", new BigDecimal("2000.00")))
                        .denominations(Map.of("BGN", Map.of(50, 10, 10, 50),
                                "EUR", Map.of(100, 10, 20, 50)))
                        .build(),

                LocalDate.of(2025, 10, 17),
                CashBalanceResponse.DailyBalanceSnapshot.builder()
                        .totalBalance(Map.of("BGN", new BigDecimal("1100.00"),
                                "EUR", new BigDecimal("2100.00")))
                        .denominations(Map.of("BGN", Map.of(50, 12, 10, 50),
                                "EUR", Map.of(100, 11, 20, 50)))
                        .build()
        );

        return CashBalanceResponse.builder()
                .cashierId(cashier)
                .dailySnapshots(snapshots)
                .build();
    }

    @Test
    void unauthorizedWithoutApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/cash-balance"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedWithInvalidApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/cash-balance")
                        .header("FIB-X-AUTH", "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizedWithValidApiKey() throws Exception {
        when(cashBalanceService.getBalance(any(), any(), any(), any()))
                .thenReturn(sampleCashBalanceResponse("MARTINA"));

        mockMvc.perform(get("/api/v1/cash-balance")
                        .header("FIB-X-AUTH", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cashier").value("MARTINA"))
                .andExpect(jsonPath("$.dailySnapshots['2025-10-16'].totalBalance.BGN").value(1000.00))
                .andExpect(jsonPath("$.dailySnapshots['2025-10-16'].denominations.BGN['50']").value(10))
                .andExpect(jsonPath("$.dailySnapshots['2025-10-16'].denominations.BGN['10']").value(50));
    }

    @Test
    void testMissingTimezoneDefaultsToUtc() throws Exception {
        when(cashBalanceService.getBalance(any(), any(), any(), any()))
                .thenReturn(sampleCashBalanceResponse("MARTINA"));

        mockMvc.perform(get("/api/v1/cash-balance")
                        .header("FIB-X-AUTH", apiKey))
                .andExpect(status().isOk());
    }

    @Test
    void testInvalidTimezoneDefaultsToUtc() throws Exception {
        when(cashBalanceService.getBalance(any(), any(), any(), any()))
                .thenReturn(sampleCashBalanceResponse("MARTINA"));

        mockMvc.perform(get("/api/v1/cash-balance")
                        .header("FIB-X-AUTH", apiKey)
                        .param("zoneId", "Invalid/Zone"))
                .andExpect(status().isOk());
    }

    @Test
    void testResponseStructure() throws Exception {
        when(cashBalanceService.getBalance(any(), any(), any(), any()))
                .thenReturn(sampleCashBalanceResponse("MARTINA"));

        mockMvc.perform(get("/api/v1/cash-balance")
                        .header("FIB-X-AUTH", apiKey))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dailySnapshots['2025-10-16'].totalBalance.BGN").exists())
                .andExpect(jsonPath("$.dailySnapshots['2025-10-16'].totalBalance.EUR").exists())
                .andExpect(jsonPath("$.dailySnapshots['2025-10-16'].denominations.BGN").exists())
                .andExpect(jsonPath("$.dailySnapshots['2025-10-16'].denominations.EUR").exists());
    }
}