package com.example.demo.dto;

import com.example.demo.annotations.ValidExpirationDate;
import jakarta.validation.constraints.Pattern;

public class UrlExpireDto {
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Expiration date must be in the format YYYY-MM-DD")
    @ValidExpirationDate
    private String expirationDate;

    public UrlExpireDto(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public UrlExpireDto() {
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "UrlExpireDto{" +
                "expirationDate='" + expirationDate + '\'' +
                '}';
    }
}
