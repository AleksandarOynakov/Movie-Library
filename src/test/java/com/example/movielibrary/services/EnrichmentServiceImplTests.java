package com.example.movielibrary.services;

import com.example.movielibrary.clients.OmdbClient;
import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.omdb.OmdbResponseDto;
import com.example.movielibrary.repositories.MovieRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

import java.time.Year;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrichmentServiceImplTests {

    private static final int MOVIE_ID = 1;
    private static final String TITLE = "The Matrix";
    private static final Year YEAR = Year.of(1999);

    @Mock
    private OmdbClient omdbClient;

    @Mock
    private MovieRepository movieRepository;

    private EnrichmentServiceImpl enrichmentService;

    @BeforeEach
    void setUp() {
        enrichmentService = new EnrichmentServiceImpl(
                omdbClient,
                movieRepository
        );
    }

    @Test
    void enrichMovie_ShouldEnrichAllMissingFields() {
        Movie movie = createMovie(null, null, null);

        OmdbResponseDto response = createSuccessfulResponse("The Wachowskis", "1999", "8.7");

        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(response);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

        enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR);

        assertEquals("The Wachowskis", movie.getDirector());
        assertEquals(Year.of(1999), movie.getYear());
        assertEquals(8.7, movie.getRating());

        verify(movieRepository).save(movie);
    }

    @Test
    void enrichMovie_ShouldNotOverwriteExistingValues() {
        Movie movie = createMovie("Existing director", Year.of(2000), 9.5);

        OmdbResponseDto response = createSuccessfulResponse("OMDb director", "1999", "8.7");

        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(response);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

        enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR);

        assertEquals("Existing director", movie.getDirector());
        assertEquals(Year.of(2000), movie.getYear());
        assertEquals(9.5, movie.getRating());

        verify(movieRepository).save(movie);
    }

    @Test
    void enrichMovie_ShouldIgnoreUnavailableOmdbValues() {
        Movie movie = createMovie(null, null, null);

        OmdbResponseDto response = createSuccessfulResponse("N/A", "N/A", "N/A");

        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(response);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

        enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR);

        assertNull(movie.getDirector());
        assertNull(movie.getYear());
        assertNull(movie.getRating());

        verify(movieRepository).save(movie);
    }

    @Test
    void enrichMovie_ShouldStop_WhenResponseIsNull() {
        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(null);

        enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR);

        verify(movieRepository, never()).findById(anyInt());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void enrichMovie_ShouldStop_WhenOmdbDoesNotFindMovie() {
        OmdbResponseDto response = new OmdbResponseDto(null, null, null, null, "False");

        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(response);

        enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR);

        verify(movieRepository, never()).findById(anyInt());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void enrichMovie_ShouldNotSave_WhenMovieWasDeleted() {
        OmdbResponseDto response = createSuccessfulResponse("The Wachowskis", "1999", "8.7");

        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(response);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.empty());

        enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR);

        verify(movieRepository, never()).save(any());
    }

    @Test
    void enrichMovie_ShouldHandleRestClientException() {
        when(omdbClient.findMovie(TITLE, YEAR)).thenThrow(new RestClientException("OMDb is unavailable"));

        assertDoesNotThrow(() -> enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR));

        verify(movieRepository, never()).findById(anyInt());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void enrichMovie_ShouldHandleInvalidRating() {
        Movie movie = createMovie(null, null, null);

        OmdbResponseDto response = createSuccessfulResponse("The Wachowskis", "1999", "invalid-rating");

        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(response);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

        assertDoesNotThrow(() -> enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR));

        assertNull(movie.getRating());
        verify(movieRepository, never()).save(any());
    }

    @Test
    void enrichMovie_ShouldHandleInvalidYear() {
        Movie movie = createMovie(null, null, 8.7);

        OmdbResponseDto response = createSuccessfulResponse("The Wachowskis", "invalid-year", "8.7");

        when(omdbClient.findMovie(TITLE, YEAR)).thenReturn(response);
        when(movieRepository.findById(MOVIE_ID)).thenReturn(Optional.of(movie));

        assertDoesNotThrow(() -> enrichmentService.enrichMovie(MOVIE_ID, TITLE, YEAR));
        assertNull(movie.getYear());

        verify(movieRepository, never()).save(any());
    }

    private Movie createMovie(String director, Year year, Double rating) {
        Movie movie = new Movie();
        movie.setId(MOVIE_ID);
        movie.setTitle(TITLE);
        movie.setDirector(director);
        movie.setYear(year);
        movie.setRating(rating);
        return movie;
    }

    private OmdbResponseDto createSuccessfulResponse(String director, String year, String rating) {
        return new OmdbResponseDto(TITLE, director, year, rating, "True");
    }
}