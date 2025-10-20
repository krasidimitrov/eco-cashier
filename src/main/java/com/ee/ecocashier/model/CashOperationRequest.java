package com.ee.ecocashier.model;

import com.ee.ecocashier.enums.CurrencyType;
import com.ee.ecocashier.enums.OperationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.Map;

@ValidDenominations
public record CashOperationRequest (
        @NotBlank String cashierId,

        @NotNull OperationType operationType,

        @NotNull CurrencyType currency,

        @NotNull @Positive BigDecimal amount,

        @NotEmpty Map<Integer, Integer> denominations
) {}
