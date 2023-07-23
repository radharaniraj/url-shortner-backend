package com.example.demo.dto;

public class DeleteUrlResponseDto {

    private String message;

    public DeleteUrlResponseDto(String message) {
        this.message = message;
    }

    public DeleteUrlResponseDto() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "DeleteUrlResponseDto{" +
                "message='" + message + '\'' +
                '}';
    }
}
