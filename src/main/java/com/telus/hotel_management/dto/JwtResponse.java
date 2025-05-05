package com.telus.hotel_management.dto;

import lombok.Data;

import java.util.List;

@Data
public class JwtResponse {

    private final String token;
    private final Long id;
    private final String username;
    private final List<String> roles;

    public JwtResponse(String jwt, Long id, String username, List<String> roles) {
        this.token = jwt;
        this.id = id;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getRoles() {
        return roles;
    }

    @Override
    public String toString() {
        return "JwtResponse{" +
                "token='" + token + '\'' +
                ", id=" + id +
                ", username='" + username + '\'' +
                ", roles=" + roles +
                '}';
    }
}
