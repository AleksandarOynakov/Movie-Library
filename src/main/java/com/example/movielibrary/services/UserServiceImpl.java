package com.example.movielibrary.services;

import com.example.movielibrary.exceptions.DuplicateEntityException;
import com.example.movielibrary.helpers.ModelMapper;
import com.example.movielibrary.models.dtos.RegisterUserDto;
import com.example.movielibrary.models.user.User;
import com.example.movielibrary.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    public User register(RegisterUserDto registerUserDto){
        if(userRepository.existsByUsername(registerUserDto.getUsername())){
            throw new DuplicateEntityException("Username already exists!");
        }
        registerUserDto.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        return userRepository.save(modelMapper.formDtoToObject(registerUserDto));
    }
}
