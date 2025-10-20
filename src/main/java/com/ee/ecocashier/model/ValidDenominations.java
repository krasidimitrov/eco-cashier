package com.ee.ecocashier.model;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DenominationValidator.class)
public @interface ValidDenominations {

    String message() default "Invalid denominations";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
