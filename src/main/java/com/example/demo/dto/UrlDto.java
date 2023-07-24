package com.example.demo.dto;

import com.example.demo.annotations.ValidExpirationDate;
import jakarta.validation.constraints.*;
import jakarta.validation.GroupSequence;


interface FirstValidationGroup {}

interface SecondValidationGroup {}

@GroupSequence({UrlDto.class, FirstValidationGroup.class, SecondValidationGroup.class})
public class UrlDto {
    @NotEmpty(message = "url is required")
    @Pattern(regexp = "^https?://.*", message = "URL must start with http:// or https://")
    private String url;
    @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Expiration date must be in the format YYYY-MM-DD",
            groups = FirstValidationGroup.class)
    @ValidExpirationDate(groups = SecondValidationGroup.class)
    private String expirationDate;

    @Pattern(regexp = "^[a-zA-Z0-9]{3,8}$", message = "Custom slug must be alphanumeric and between 3 to 8 characters")
    private String customSlug;

    public UrlDto(String url, String expirationDate) {
        this.url = url;
        this.expirationDate = expirationDate;
    }

    public UrlDto() {
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
