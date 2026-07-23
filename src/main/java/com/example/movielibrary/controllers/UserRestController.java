package com.example.movielibrary.controllers;

import com.example.movielibrary.models.user.userDtos.RegisterUserDto;
import com.example.movielibrary.models.user.userDtos.ResponseUserDto;
import com.example.movielibrary.models.user.ApplicationUser;
import com.example.movielibrary.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User registration and authentication")
public class UserRestController {
    private final UserService userService;

    @Autowired
    public UserRestController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Register a new user",
            description = "Creates a new account with the USER role"
    )
    public ResponseUserDto register(@Valid @RequestBody RegisterUserDto registerUserDto){
        ApplicationUser applicationUser = userService.register(registerUserDto);
        return new ResponseUserDto(applicationUser.getId(), applicationUser.getUsername(), applicationUser.getRole());
    }
}
