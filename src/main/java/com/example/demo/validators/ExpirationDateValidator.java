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
            return true; // Null values are handled by @NotEmpty annotation
        }

        LocalDate currentDate = LocalDate.now();
        LocalDate parsedExpirationDate = LocalDate.parse(expirationDate);

        return parsedExpirationDate.isAfter(currentDate);
    }
}
