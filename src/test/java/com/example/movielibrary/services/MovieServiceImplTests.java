package com.example.movielibrary.services;

import com.example.movielibrary.exceptions.DuplicateEntityException;
import com.example.movielibrary.helpers.ModelMapper;
import com.example.movielibrary.helpers.NullChecker;
import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.models.movie.movieDtos.UpdateMovieDto;
import com.example.movielibrary.repositories.MovieRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceImplTests {

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private NullChecker nullChecker;

    @Mock
    private EnrichmentService enrichmentService;

    private MovieServiceImpl movieService;

    @BeforeEach
    void setUp() {
        movieService = new MovieServiceImpl(
                movieRepository,
                modelMapper,
                nullChecker,
                enrichmentService
        );
    }

    @Test
    void getAll_ShouldReturnAllMovies() {
        Movie firstMovie = createMovie(1, "The Matrix", "The Wachowskis", 1999, 8.7);
        Movie secondMovie = createMovie(2, "Interstellar", "Christopher Nolan", 2014, 8.7);

        when(movieRepository.findAll()).thenReturn(List.of(firstMovie, secondMovie));

        List<Movie> result = movieService.getAll();

        assertEquals(2, result.size());
        assertSame(firstMovie, result.get(0));
        assertSame(secondMovie, result.get(1));

        verify(movieRepository).findAll();
    }

    @Test
    void getById_ShouldReturnMovie_WhenMovieExists() {
        Movie movie = createMovie(1, "The Matrix", "The Wachowskis", 1999, 8.7);

        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));

        Movie result = movieService.getById(1);

        assertSame(movie, result);
        verify(movieRepository).findById(1);
    }

    @Test
    void getById_ShouldThrow_WhenMovieDoesNotExist() {
        when(movieRepository.findById(99)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> movieService.getById(99));

        assertEquals("Movie with id 99 was not found", exception.getMessage());
    }

    @Test
    void create_ShouldTrimTitleAndSaveMovie_WhenTitleIsUnique() {
        CreateMovieDto dto = new CreateMovieDto();

        Movie movie = createMovie(1, "  The Matrix  ", "The Wachowskis", 1999, 8.7);

        when(modelMapper.fromDtoToObject(dto)).thenReturn(movie);
        when(movieRepository.existsByTitleIgnoreCase("The Matrix")).thenReturn(false);
        when(movieRepository.save(movie)).thenReturn(movie);
        when(nullChecker.containsNullValue(movie)).thenReturn(false);

        Movie result = movieService.create(dto);

        assertSame(movie, result);
        assertEquals("The Matrix", result.getTitle());

        verify(movieRepository).existsByTitleIgnoreCase("The Matrix");
        verify(movieRepository).save(movie);
        verify(enrichmentService, never()).enrichMovie(anyInt(), anyString(), any());
    }

    @Test
    void create_ShouldThrow_WhenTitleAlreadyExists() {
        CreateMovieDto dto = new CreateMovieDto();

        Movie movie = createMovie(0, "The Matrix", null, null, null);

        when(modelMapper.fromDtoToObject(dto)).thenReturn(movie);
        when(movieRepository.existsByTitleIgnoreCase("The Matrix")).thenReturn(true);

        DuplicateEntityException exception = assertThrows(DuplicateEntityException.class, () -> movieService.create(dto));

        assertEquals("Movie with title The Matrix already exists!", exception.getMessage());

        verify(movieRepository, never()).save(any());
        verifyNoInteractions(enrichmentService);
    }

    @Test
    void create_ShouldStartEnrichment_WhenMetadataIsMissing() {
        CreateMovieDto dto = new CreateMovieDto();

        Movie movie = createMovie(1, "The Matrix", null, null, null);

        when(modelMapper.fromDtoToObject(dto)).thenReturn(movie);
        when(movieRepository.existsByTitleIgnoreCase("The Matrix")).thenReturn(false);
        when(movieRepository.save(movie)).thenReturn(movie);
        when(nullChecker.containsNullValue(movie)).thenReturn(true);

        Movie result = movieService.create(dto);

        assertSame(movie, result);

        verify(enrichmentService).enrichMovie(1, "The Matrix", null);
    }

    @Test
    void update_ShouldReplaceAndSaveMovie() {
        Movie existingMovie = createMovie(1, "Old title", "Old director", 2000, 5.0);
        Movie updatedMovie = createMovie(1, "New title", "New director", 2020, 9.0);

        UpdateMovieDto dto = new UpdateMovieDto();

        when(movieRepository.findById(1)).thenReturn(Optional.of(existingMovie));
        when(modelMapper.fromDtoToObject(existingMovie, dto)).thenReturn(updatedMovie);
        when(movieRepository.save(updatedMovie)).thenReturn(updatedMovie);
        when(nullChecker.containsNullValue(updatedMovie)).thenReturn(false);

        Movie result = movieService.update(1, dto);

        assertSame(updatedMovie, result);

        verify(movieRepository).save(updatedMovie);
        verify(enrichmentService, never()).enrichMovie(anyInt(), anyString(), any());
    }

    @Test
    void update_ShouldStartEnrichment_WhenMetadataIsMissing() {
        Movie existingMovie = createMovie(1, "The Matrix", "Old director", 1999, 8.0);
        Movie updatedMovie = createMovie(1, "The Matrix", null, null, null);

        UpdateMovieDto dto = new UpdateMovieDto();

        when(movieRepository.findById(1)).thenReturn(Optional.of(existingMovie));
        when(modelMapper.fromDtoToObject(existingMovie, dto)).thenReturn(updatedMovie);
        when(movieRepository.save(updatedMovie)).thenReturn(updatedMovie);
        when(nullChecker.containsNullValue(updatedMovie)).thenReturn(true);

        movieService.update(1, dto);

        verify(enrichmentService).enrichMovie(1, "The Matrix", null);
    }

    @Test
    void delete_ShouldDeleteMovie_WhenMovieExists() {
        Movie movie = createMovie(1, "The Matrix", "The Wachowskis", 1999, 8.7);

        when(movieRepository.findById(1)).thenReturn(Optional.of(movie));

        movieService.delete(1);

        verify(movieRepository).delete(movie);
    }

    @Test
    void delete_ShouldThrowAndNotDelete_WhenMovieDoesNotExist() {
        when(movieRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> movieService.delete(99));

        verify(movieRepository, never()).delete(any());
    }

    private Movie createMovie(int id, String title, String director, Integer year, Double rating) {
        Movie movie = new Movie();
        movie.setId(id);
        movie.setTitle(title);
        movie.setDirector(director);
        movie.setYear(year == null ? null : Year.of(year));
        movie.setRating(rating);
        return movie;
    }
}