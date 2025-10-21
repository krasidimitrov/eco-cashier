package com.ee.ecocashier.model;

import com.ee.ecocashier.enums.CurrencyType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Set;

public class DenominationValidator implements ConstraintValidator<ValidDenominations, CashOperationRequest> {

    private static final Set<Integer> ALLOWED_BGN_DENOMINATIONS = Set.of(1, 2, 5, 10, 20, 50, 100);
    private static final Set<Integer> ALLOWED_EUR_DENOMINATIONS = Set.of(1, 2, 5, 10, 20, 50, 100, 200, 500);

    @Override
    public boolean isValid(CashOperationRequest request, ConstraintValidatorContext context) {
        Set<Integer> allowedDenominations;

        if (request.currency() == CurrencyType.BGN) {
            allowedDenominations = ALLOWED_BGN_DENOMINATIONS;
        } else if (request.currency() == CurrencyType.EUR) {
            allowedDenominations = ALLOWED_EUR_DENOMINATIONS;
        } else {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Unsupported currency: " + request.currency())
                    .addConstraintViolation();
            return false;
        }

        int sum = 0;
        for (Map.Entry<Integer, Integer> entry : request.denominations().entrySet()) {
            if (!allowedDenominations.contains(entry.getKey())) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Invalid denomination: " + entry.getKey())
                        .addConstraintViolation();
                return false;
            }
            if (entry.getValue() <= 0) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Denomination count cannot be 0 or less!")
                        .addConstraintViolation();
                return false;
            }

            sum += entry.getKey() * entry.getValue();
        }
        if (request.amount().compareTo(BigDecimal.valueOf(sum)) != 0) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Total value of denominations is " + sum + ", but the operation amount is " + request.amount())
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
