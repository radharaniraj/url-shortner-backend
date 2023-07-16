package com.example.demo.dto;

public class UserResponseDto {
    private Long id;
    private String username;

    public UserResponseDto(Long id, String username) {
        this.id = id;
        this.username = username;
    }

    public UserResponseDto() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "UserResponseDto{" +
                "id=" + id +
                ", username='" + username + '\'' +
                '}';
    }
}
