package com.example.movielibrary.services;

import com.example.movielibrary.exceptions.DuplicateEntityException;
import com.example.movielibrary.helpers.ModelMapper;
import com.example.movielibrary.models.user.ApplicationUser;
import com.example.movielibrary.models.user.Role;
import com.example.movielibrary.models.user.userDtos.RegisterUserDto;
import com.example.movielibrary.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {
        userService = new UserServiceImpl(
                userRepository,
                passwordEncoder,
                modelMapper
        );
    }

    @Test
    void register_ShouldEncodePasswordAndSaveUser() {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("testuser");
        dto.setPassword("password");

        ApplicationUser user = new ApplicationUser();
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encoded-password");
        when(modelMapper.formDtoToObject(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        ApplicationUser result = userService.register(dto);

        assertSame(user, result);
        assertEquals("encoded-password", dto.getPassword());

        verify(passwordEncoder).encode("password");
        verify(modelMapper).formDtoToObject(dto);
        verify(userRepository).save(user);
    }

    @Test
    void register_ShouldThrow_WhenUsernameAlreadyExists() {
        RegisterUserDto dto = new RegisterUserDto();
        dto.setUsername("testuser");
        dto.setPassword("password");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        DuplicateEntityException exception = assertThrows(DuplicateEntityException.class, () -> userService.register(dto));

        assertEquals("Username already exists!", exception.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(modelMapper, never()).formDtoToObject(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_ShouldReturnUserDetails_WhenUserExists() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("admin");
        user.setPassword("encoded-password");
        user.setRole(Role.ADMIN);

        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        UserDetails result = userService.loadUserByUsername("admin");

        assertEquals("admin", result.getUsername());
        assertEquals("encoded-password", result.getPassword());

        assertTrue(result.getAuthorities().stream()
                        .anyMatch(authority ->
                                authority.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void loadUserByUsername_ShouldThrow_WhenUserDoesNotExist() {
        when(userRepository.findByUsername("missing")).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("missing"));

        assertEquals("Invalid username or password!", exception.getMessage());
    }
}