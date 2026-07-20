package com.example.movielibrary.helpers;

import com.example.movielibrary.models.dtos.RegisterUserDto;
import com.example.movielibrary.models.user.Role;
import com.example.movielibrary.models.user.ApplicationUser;
import org.springframework.stereotype.Component;

@Component
public class ModelMapper {
    public ApplicationUser formDtoToObject(RegisterUserDto registerUserDto){
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername(registerUserDto.getUsername());
        applicationUser.setPassword(registerUserDto.getPassword());
        applicationUser.setRole(Role.USER);
        return applicationUser;
    }
}
