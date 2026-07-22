package com.example.movielibrary.controllers;

import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.models.movie.movieDtos.UpdateMovieDto;
import com.example.movielibrary.services.MovieService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
public class MovieRestController {
    private final MovieService movieService;

    @Autowired
    public MovieRestController(MovieService movieService) {
        this.movieService = movieService;
    }

    @GetMapping
    public List<Movie> getAll(){
        return movieService.getAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Movie create(@Valid @RequestBody CreateMovieDto createMovieDto){
        return movieService.create(createMovieDto);
    }

    @GetMapping("/{movieId}")
    public Movie getById(@PathVariable int movieId){
        return movieService.getById(movieId);
    }

    @PutMapping("/{movieId}")
    public Movie update(@PathVariable int movieId, @Valid @RequestBody UpdateMovieDto updateMovieDto){
        return movieService.update(movieId,updateMovieDto);
    }

    @DeleteMapping("/{movieId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable int movieId){
        movieService.delete(movieId);
    }
}
