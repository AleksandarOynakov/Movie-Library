package com.example.movielibrary.models.user.userDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterUserDto {
    @NotBlank(message = "Username must not be blank")
    @Size(min = 4, max = 40, message = "Username must be min 4 and max 40 symbols")
    private String username;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 4, max = 40, message = "Password must be min 4 and max 40 symbols")
    private String password;

    public String getUsername() {
        return username;
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
}
