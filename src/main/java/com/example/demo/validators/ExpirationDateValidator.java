package com.example.demo.validators;

import com.example.demo.annotations.ValidExpirationDate;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ExpirationDateValidator implements ConstraintValidator<ValidExpirationDate, String> {
    @Override
    public void initialize(ValidExpirationDate constraintAnnotation) {
        // No initialization required
    }

    @Override
    public boolean isValid(String expirationDate, ConstraintValidatorContext context) {
        if (expirationDate == null) {
            return true;
        }

        LocalDate currentDate = LocalDate.now();
        try {
            LocalDate parsedExpirationDate = LocalDate.parse(expirationDate);
            return parsedExpirationDate.isAfter(currentDate);
        }
        catch (Exception e) {
            return false;
        }
    }
}
