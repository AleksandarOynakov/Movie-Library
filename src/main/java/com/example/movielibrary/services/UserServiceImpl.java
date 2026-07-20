package com.example.movielibrary.services;

import com.example.movielibrary.exceptions.DuplicateEntityException;
import com.example.movielibrary.helpers.ModelMapper;
import com.example.movielibrary.models.user.userDtos.RegisterUserDto;
import com.example.movielibrary.models.user.ApplicationUser;
import com.example.movielibrary.repositories.UserRepository;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    @Override
    public ApplicationUser register(RegisterUserDto registerUserDto) {
        if (userRepository.existsByUsername(registerUserDto.getUsername())) {
            throw new DuplicateEntityException("Username already exists!");
        }
        registerUserDto.setPassword(passwordEncoder.encode(registerUserDto.getPassword()));
        return userRepository.save(modelMapper.formDtoToObject(registerUserDto));
    }

    @NullMarked
    @Override
    public UserDetails loadUserByUsername(String username) {
        ApplicationUser applicationUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password!"));

        return User.withUsername(applicationUser.getUsername())
                .password(applicationUser.getPassword())
                .roles(applicationUser.getRole().name())
                .build();
    }
}
