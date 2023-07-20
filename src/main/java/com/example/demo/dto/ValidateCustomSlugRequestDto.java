package com.example.demo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public class ValidateCustomSlugRequestDto {
    @NotEmpty(message = "Custom slug is required")
    @Pattern(regexp = "^[a-zA-Z0-9]{3,8}$", message = "Custom slug must be alphanumeric and between" +
            " 3 to 8 characters")
    private String customSlug;

    public ValidateCustomSlugRequestDto(String customSlug) {
        this.customSlug = customSlug;
    }

    public ValidateCustomSlugRequestDto() {
    }

    public String getCustomSlug() {
        return customSlug;
    }

    public void setCustomSlug(String customSlug) {
        this.customSlug = customSlug;
    }
}
