package com.example.movielibrary.controllers;

import com.example.movielibrary.exceptions.DuplicateEntityException;
import com.example.movielibrary.exceptions.GlobalRestExceptionHandler;
import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.models.movie.movieDtos.UpdateMovieDto;
import com.example.movielibrary.services.MovieService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Year;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class MovieRestControllerTests {

    @Mock
    private MovieService movieService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MovieRestController controller = new MovieRestController(movieService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).setControllerAdvice(new GlobalRestExceptionHandler()).build();
    }

    @Test
    void getAll_ShouldReturnMovies() throws Exception {
        Movie firstMovie = createMovie(1, "The Matrix", "The Wachowskis", 1999, 8.7);
        Movie secondMovie = createMovie(2, "Interstellar", "Christopher Nolan", 2014, 8.7);

        when(movieService.getAll()).thenReturn(List.of(firstMovie, secondMovie));

        mockMvc.perform(get("/api/movies")).andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("The Matrix"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Interstellar"));

        verify(movieService).getAll();
    }

    @Test
    void getById_ShouldReturnMovie_WhenMovieExists() throws Exception {
        Movie movie = createMovie(1, "The Matrix", "The Wachowskis", 1999, 8.7);

        when(movieService.getById(1)).thenReturn(movie);

        mockMvc.perform(get("/api/movies/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Matrix"))
                .andExpect(jsonPath("$.director").value("The Wachowskis"))
                .andExpect(jsonPath("$.rating").value(8.7));

        verify(movieService).getById(1);
    }

    @Test
    void getById_ShouldReturnNotFound_WhenMovieDoesNotExist() throws Exception {
        when(movieService.getById(99)).thenThrow(new EntityNotFoundException("Movie with id 99 was not found"));

        mockMvc.perform(get("/api/movies/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Movie with id 99 was not found"));
    }

    @Test
    void create_ShouldReturnCreatedMovie() throws Exception {
        Movie movie = createMovie(1, "The Matrix", "The Wachowskis", 1999, 8.7);

        when(movieService.create(any(CreateMovieDto.class))).thenReturn(movie);

        String requestBody = """
                {
                    "title": "The Matrix",
                    "director": "The Wachowskis",
                    "year": "1999",
                    "rating": 8.7
                }
                """;

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Matrix"))
                .andExpect(jsonPath("$.rating").value(8.7));

        verify(movieService).create(any(CreateMovieDto.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenTitleIsBlank() throws Exception {

        String requestBody = """
                {
                    "title": "     ",
                    "director": "The Wachowskis",
                    "year": "1999",
                    "rating": 8.7
                }
                """;

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Title must not be blank"));

        verify(movieService, never()).create(any(CreateMovieDto.class));
    }

    @Test
    void create_ShouldReturnBadRequest_WhenTitleIsShort() throws Exception {

        String requestBody = """
                {
                    "title": "abc",
                    "director": "The Wachowskis",
                    "year": "1999",
                    "rating": 8.7
                }
                """;

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Title must be min 4 and max 40 symbols"));

        verify(movieService, never()).create(any(CreateMovieDto.class));
    }

    @Test
    void create_ShouldReturnConflict_WhenTitleAlreadyExists() throws Exception {

        when(movieService.create(any(CreateMovieDto.class))).thenThrow(
                new DuplicateEntityException("Movie with title The Matrix already exists!"));

        String requestBody = """
                {
                    "title": "The Matrix",
                    "director": "The Wachowskis",
                    "year": "1999",
                    "rating": 8.7
                }
                """;

        mockMvc.perform(post("/api/movies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("Movie with title The Matrix already exists!"));
    }

    @Test
    void update_ShouldReturnUpdatedMovie() throws Exception {
        Movie updatedMovie = createMovie(1, "The Matrix Reloaded", "The Wachowskis", 2003, 7.2);

        when(movieService.update(eq(1), any(UpdateMovieDto.class))).thenReturn(updatedMovie);

        String requestBody = """
                {
                    "title": "The Matrix Reloaded",
                    "director": "The Wachowskis",
                    "year": "2003",
                    "rating": 7.2
                }
                """;

        mockMvc.perform(put("/api/movies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Matrix Reloaded"))
                .andExpect(jsonPath("$.year").value("2003"))
                .andExpect(jsonPath("$.rating").value(7.2));

        verify(movieService).update(eq(1), any(UpdateMovieDto.class));
    }

    @Test
    void delete_ShouldReturnNoContent() throws Exception {
        doNothing().when(movieService).delete(1);

        mockMvc.perform(delete("/api/movies/1"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(movieService).delete(1);
    }

    private Movie createMovie(int id, String title, String director, int year, double rating) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setDirector(director);
        movie.setYear(Year.of(year));
        movie.setRating(rating);
        return movie;
    }
}