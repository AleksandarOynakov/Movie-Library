package com.example.movielibrary.services;

import com.example.movielibrary.models.dtos.RegisterUserDto;
import com.example.movielibrary.models.user.ApplicationUser;

public interface UserService {
    ApplicationUser register(RegisterUserDto registerUserDto);
}
