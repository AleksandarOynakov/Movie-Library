package com.example.movielibrary.services;

import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.models.movie.movieDtos.UpdateMovieDto;

import java.util.List;

public interface MovieService {
    List<Movie> getAll();

    Movie create(CreateMovieDto createMovieDto);

    Movie getById(int id);

    Movie update(int id, UpdateMovieDto updateMovieDto);

    void delete(int id);
}
