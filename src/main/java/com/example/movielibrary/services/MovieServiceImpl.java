package com.example.movielibrary.services;

import com.example.movielibrary.exceptions.DuplicateEntityException;
import com.example.movielibrary.helpers.ModelMapper;
import com.example.movielibrary.helpers.NullChecker;
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
    private final NullChecker nullChecker;
    private final EnrichmentService enrichmentService;

    @Autowired
    public MovieServiceImpl(
            MovieRepository movieRepository,
            ModelMapper modelMapper,
            NullChecker nullChecker,
            EnrichmentService enrichmentService
    ) {
        this.movieRepository = movieRepository;
        this.modelMapper = modelMapper;
        this.nullChecker = nullChecker;
        this.enrichmentService = enrichmentService;
    }

    @Override
    public List<Movie> getAll() {
        return movieRepository.findAll();
    }

    @Override
    public Movie create(CreateMovieDto createMovieDto) {
        Movie movie = modelMapper.fromDtoToObject(createMovieDto);
        String title = movie.getTitle().trim();

        if (movieRepository.existsByTitleIgnoreCase(title)) {
            throw new DuplicateEntityException(String.format("Movie with title %s already exists!", movie.getTitle()));
        }

        Movie savedMovie = movieRepository.save(movie);
        if (nullChecker.containsNullValue(savedMovie)) {
            enrichmentService.enrichMovie(savedMovie.getId(), savedMovie.getTitle(), savedMovie.getYear());
        }
        return savedMovie;
    }

    @Override
    public Movie getById(int id) {
        return movieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Movie with id %d was not found", id)));
    }

    @Override
    public Movie update(int id, UpdateMovieDto updateMovieDto) {
        Movie movie = getById(id);
        Movie savedMovie = movieRepository.save(modelMapper.fromDtoToObject(movie, updateMovieDto));
        if (nullChecker.containsNullValue(movie)) {
            enrichmentService.enrichMovie(savedMovie.getId(), savedMovie.getTitle(), savedMovie.getYear());
        }
        return savedMovie;
    }

    @Override
    public void delete(int id) {
        Movie movie = getById(id);
        movieRepository.delete(movie);
    }

}
