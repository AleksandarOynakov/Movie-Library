package com.example.movielibrary.helpers;

import com.example.movielibrary.models.dtos.RegisterUserDto;
import com.example.movielibrary.models.user.Role;
import com.example.movielibrary.models.user.User;
import org.springframework.stereotype.Component;

@Component
public class ModelMapper {
    public User formDtoToObject(RegisterUserDto registerUserDto){
        User user = new User();
        user.setUsername(registerUserDto.getUsername());
        user.setPassword(registerUserDto.getPassword());
        user.setRole(Role.USER);
        return user;
    }
}
