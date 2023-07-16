package com.example.demo.dto;

public class ValidateCustomSlugResponseDto {
    private String message;

    public ValidateCustomSlugResponseDto(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
