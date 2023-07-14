package com.example.demo.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UrlDto {
    @NotEmpty(message = "url is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Expiration date must be in the format yyyy-MM-dd")
    private String expirationDate;

    @Pattern(regexp = "^[a-zA-Z0-9]{3,8}$", message = "Custom slug must be alphanumeric and between 3 to 8 characters")
    private String customSlug;

    public UrlDto(String url, String expirationDate) {
        this.url = url;
        this.expirationDate = expirationDate;
    }

    public String getUrl() {
        return url;
    }

    public String getCustomSlug() {
        return customSlug;
    }

    @Override
    public String toString() {
        return "UrlDto{" +
                "url='" + url + '\'' +
                ", expirationDate='" + expirationDate + '\'' +
                ", customSlug='" + customSlug + '\'' +
                '}';
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setCustomSlug(String customSlug) {
        this.customSlug = customSlug;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

}
