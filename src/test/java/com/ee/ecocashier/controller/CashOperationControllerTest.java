package com.ee.ecocashier.controller;

import com.ee.ecocashier.enums.CurrencyType;
import com.ee.ecocashier.enums.OperationType;
import com.ee.ecocashier.model.CashOperationRequest;
import com.ee.ecocashier.service.CashOperationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CashOperationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CashOperationService cashBalanceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${api.key}")
    private String apiKey;

    @Autowired
    private CashOperationService cashOperationService;

    @Test
    void unauthorizedWithoutApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/cash-operation"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void unauthorizedWithInvalidApiKey() throws Exception {
        mockMvc.perform(get("/api/v1/cash-operation")
                        .header("FIB-X-AUTH", "wrong-key"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void authorizedWithValidApiKey() throws Exception {
        mockMvc.perform(post("/api/v1/cash-operation")
                        .header("FIB-X-AUTH", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dummyRequest())))
                .andExpect(status().isOk());

        verify(cashOperationService, times(1)).handleOperation(any());
    }

    private CashOperationRequest dummyRequest() {
        return new CashOperationRequest(
                "MARTINA",
                OperationType.DEPOSIT,
                CurrencyType.BGN,
                BigDecimal.valueOf(100),
                Map.of(50, 1)
        );
    }
}