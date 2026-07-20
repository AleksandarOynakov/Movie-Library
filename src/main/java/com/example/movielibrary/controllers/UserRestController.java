package com.example.movielibrary.controllers;

import com.example.movielibrary.models.dtos.RegisterUserDto;
import com.example.movielibrary.models.dtos.ResponseUserDto;
import com.example.movielibrary.models.user.User;
import com.example.movielibrary.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserRestController {
    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseUserDto register(@Valid @RequestBody RegisterUserDto registerUserDto){
        User user = userService.register(registerUserDto);
        return new ResponseUserDto(user.getId(), user.getUsername(), user.getRole());
    }
}
