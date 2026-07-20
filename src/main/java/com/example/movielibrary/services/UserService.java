package com.example.movielibrary.services;

import com.example.movielibrary.models.dtos.RegisterUserDto;
import com.example.movielibrary.models.user.User;

public interface UserService {
    User register(RegisterUserDto registerUserDto);
}
