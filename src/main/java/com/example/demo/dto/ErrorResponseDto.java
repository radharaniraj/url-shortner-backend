package com.example.demo.dto;

public class ErrorResponseDto {
    private String status;
    private String error;

    public ErrorResponseDto(String status, String error) {
        this.status = status;
        this.error = error;
    }

    public ErrorResponseDto() {

    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "urlErrorResponseDto{" +
                "status='" + status + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
