package com.example.movielibrary.controllers;

import com.example.movielibrary.exceptions.DuplicateEntityException;
import com.example.movielibrary.exceptions.GlobalRestExceptionHandler;
import com.example.movielibrary.models.user.ApplicationUser;
import com.example.movielibrary.models.user.Role;
import com.example.movielibrary.models.user.userDtos.RegisterUserDto;
import com.example.movielibrary.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTests {

    @Mock
    private UserService userService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        UserRestController controller = new UserRestController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalRestExceptionHandler()).build();
    }

    @Test
    void register_ShouldReturnCreatedUser_WhenRequestIsValid() throws Exception {
        ApplicationUser registeredUser = createUser();

        when(userService.register(any(RegisterUserDto.class))).thenReturn(registeredUser);

        String requestBody = """
                {
                    "username": "testuser",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.password").doesNotExist());

        verify(userService).register(any(RegisterUserDto.class));
    }

    @Test
    void register_ShouldReturnBadRequest_WhenUsernameIsBlank() throws Exception {
        String requestBody = """
                {
                    "username": "    ",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Username must not be blank"));

        verifyNoInteractions(userService);
    }

    @Test
    void register_ShouldReturnBadRequest_WhenPasswordIsTooShort() throws Exception {
        String requestBody = """
                {
                    "username": "testuser",
                    "password": "abc"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Password must be min 4 and max 40 symbols"));

        verifyNoInteractions(userService);
    }

    @Test
    void register_ShouldReturnConflict_WhenUsernameAlreadyExists() throws Exception {
        when(userService.register(any(RegisterUserDto.class))).thenThrow(new DuplicateEntityException("Username already exists!"));

        String requestBody = """
                {
                    "username": "testuser",
                    "password": "password"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Username already exists!"));

        verify(userService).register(any(RegisterUserDto.class));
    }

    private ApplicationUser createUser() {
        ApplicationUser user = new ApplicationUser();
        user.setId(1);
        user.setUsername("testuser");
        user.setPassword("encoded-password");
        user.setRole(Role.USER);
        return user;
    }
}