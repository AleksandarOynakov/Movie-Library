package com.example.movielibrary.security;

import com.example.movielibrary.configuration.SecurityConfiguration;
import com.example.movielibrary.controllers.MovieRestController;
import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.services.MovieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Year;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieRestController.class)
@Import({
        SecurityConfiguration.class,
        SecurityIntegrationTests.EnableSecurity.class
})
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MovieService movieService;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @TestConfiguration
    @EnableWebSecurity
    static class EnableSecurity {
    }

    @Test
    void getMovies_ShouldReturn401_WhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/movies"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("You must be logged in to access this resource."));

        verifyNoInteractions(movieService);
    }

    @Test
    void getMovies_ShouldReturn200_WhenLoggedInAsUser() throws Exception {
        when(movieService.getAll()).thenReturn(List.of(createMovie()));

        mockMvc.perform(get("/api/movies").with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("The Matrix"));

        verify(movieService).getAll();
    }

    @Test
    void createMovie_ShouldReturn403_WhenLoggedInAsUser() throws Exception {
        mockMvc.perform(post("/api/movies")
                        .with(user("user").roles("USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovieJson()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("You must be ADMIN to access this resource."));

        verifyNoInteractions(movieService);
    }

    @Test
    void createMovie_ShouldReturn201_WhenLoggedInAsAdmin() throws Exception {
        when(movieService.create(any(CreateMovieDto.class))).thenReturn(createMovie());

        mockMvc.perform(post("/api/movies").with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validMovieJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("The Matrix"));

        verify(movieService).create(any(CreateMovieDto.class));
    }

    private Movie createMovie() {
        Movie movie = new Movie();
        movie.setId(1);
        movie.setTitle("The Matrix");
        movie.setDirector("The Wachowskis");
        movie.setYear(Year.of(1999));
        movie.setRating(8.7);
        return movie;
    }

    private String validMovieJson() {
        return """
                {
                    "title": "The Matrix",
                    "director": "The Wachowskis",
                    "year": "1999",
                    "rating": 8.7
                }
                """;
    }
}