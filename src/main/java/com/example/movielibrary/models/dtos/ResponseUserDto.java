package com.example.movielibrary.models.dtos;

import com.example.movielibrary.models.user.Role;

public class ResponseUserDto {
    private int id;
    private String username;
    private Role role;

    public ResponseUserDto(int id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
