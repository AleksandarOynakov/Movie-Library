package com.example.movielibrary.services;

import com.example.movielibrary.helpers.ModelMapper;
import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.models.movie.movieDtos.UpdateMovieDto;
import com.example.movielibrary.repositories.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public MovieServiceImpl(MovieRepository movieRepository, ModelMapper modelMapper) {
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    @Override
    public Movie create(CreateMovieDto createMovieDto) {
        Movie movie = modelMapper.fromDtoToObject(createMovieDto);
        //Will possible add enrichment here
        return movieRepository.save(movie);
    }

    @Override
    public Movie getById(int id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Movie with id %d was not found", id)));
    }

    @Override
    public Movie update(int id, UpdateMovieDto updateMovieDto) {
        Movie movie = getById(id);
        return movieRepository.save(modelMapper.fromDtoToObject(movie, updateMovieDto));
    }

    @Override
    public void delete(int id){
        Movie movie = getById(id);
        movieRepository.delete(movie);
    }

}
