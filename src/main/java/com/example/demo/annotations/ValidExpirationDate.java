package com.example.demo.annotations;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import com.example.demo.validators.ExpirationDateValidator;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = ExpirationDateValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidExpirationDate {
    String message() default "Expiration date must be greater than today's date";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
