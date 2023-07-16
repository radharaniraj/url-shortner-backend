package com.example.demo.dto;

public class UserRequestDto {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "UserRequestDto{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRequestDto(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
