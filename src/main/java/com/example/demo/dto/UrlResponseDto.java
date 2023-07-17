package com.example.demo.dto;

import java.time.LocalDateTime;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class UrlResponseDto {
    private String originalUrl;
    private String shortLink;
    private LocalDateTime expirationDate;

    public UrlResponseDto() {

    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }



    public UrlResponseDto(String originalurl, String shortLink, LocalDateTime expirationDate) {
        this.originalUrl = originalurl;
        this.shortLink = shortLink;
        this.expirationDate = expirationDate;
    }



    public String getShortLink() {
        return shortLink;
    }

    public void setShortLink(String shortLink) {
        this.shortLink = shortLink;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public String toString() {
        return "UrlResponseDto{" +
                "originalurl='" + originalUrl + '\'' +
                ", shortLink='" + shortLink + '\'' +
                ", expirationDate=" + expirationDate +
                '}';
    }


}
