package com.example.movielibrary.controllers;

import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.models.movie.movieDtos.UpdateMovieDto;
import com.example.movielibrary.services.MovieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movies", description = "Operations for managing the movie library")
@SecurityRequirement(name = "basicAuth")
public class MovieRestController {
    private final MovieService movieService;

    @Autowired
    public MovieRestController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    @Operation(
            summary = "List all movies",
            description = "Requires the USER or ADMIN role"
    )
    public List<Movie> getAll(){
        return movieService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a movie",
            description = "Requires the ADMIN role. Missing metadata is enriched asynchronously through OMDb."
    )
    public Movie create(@Valid @RequestBody CreateMovieDto createMovieDto){
        return movieService.create(createMovieDto);
    }

    @GetMapping("/{movieId}")
    @Operation(
            summary = "Get a movie by ID",
            description = "Requires the USER or ADMIN role"
    )
    public Movie getById(@PathVariable int movieId){
        return movieService.getById(movieId);
    }

    @PutMapping("/{movieId}")
    @Operation(
            summary = "Update a movie",
            description = "Requires the ADMIN role"
    )
    public Movie update(@PathVariable int movieId, @Valid @RequestBody UpdateMovieDto updateMovieDto){
        return movieService.update(movieId,updateMovieDto);
    }

    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete a movie",
            description = "Requires the ADMIN role"
    )
    public void delete(@PathVariable int movieId){
        movieService.delete(movieId);
    }
}
