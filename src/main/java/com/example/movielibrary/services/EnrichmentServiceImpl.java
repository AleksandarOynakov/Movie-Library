package com.example.movielibrary.services;

import com.example.movielibrary.clients.OmdbClient;
import com.example.movielibrary.models.omdb.OmdbResponseDto;
import com.example.movielibrary.repositories.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.Year;
import java.time.format.DateTimeParseException;

@Service
public class EnrichmentServiceImpl implements EnrichmentService {
    private static final Logger logger = LoggerFactory.getLogger(EnrichmentServiceImpl.class);
    private final OmdbClient omdbClient;
    private final MovieRepository movieRepository;

    public EnrichmentServiceImpl(OmdbClient omdbClient, MovieRepository movieRepository) {
        this.omdbClient = omdbClient;
        this.movieRepository = movieRepository;
    }

    @Async
    @Override
    public void enrichMovie(int movieId, String title, Year year) {
        try {
            OmdbResponseDto responseDto = omdbClient.findMovie(title, year);
            if (responseDto == null || !responseDto.wasFound()) {
                logger.warn("OMDb didn't find movie with title {} ({})", title, year);
                return;
            }

            String ratingFromResponse = responseDto.imdbRating();
            String directorFromResponse = responseDto.director();
            String yearFromResponse = responseDto.year();

            if (ratingFromResponse == null || ratingFromResponse.equalsIgnoreCase("N/A")) {
                logger.warn("OMDb didn't return rating for movie with title {} ({})", title, year);
            }

            if (directorFromResponse == null || directorFromResponse.equalsIgnoreCase("N/A")) {
                logger.warn("OMDb didn't return director for movie with title {} ({})", title, year);
            }

            if (yearFromResponse == null || yearFromResponse.equalsIgnoreCase("N/A")) {
                logger.warn("OMDb didn't return year for movie with title {} ({})", title, year);
            }

            movieRepository.findById(movieId)
                    .ifPresentOrElse(movie -> {

                                //Set rating
                                if (movie.getRating() == null &&
                                        ratingFromResponse != null &&
                                        !ratingFromResponse.equalsIgnoreCase("N/A")) {
                                    movie.setRating(Double.parseDouble(ratingFromResponse));
                                    logger.info("Rating for movie with id {} enriched with {}", movieId, movie.getRating());
                                }

                                //Set director
                                if (movie.getDirector() == null &&
                                        directorFromResponse != null &&
                                        !directorFromResponse.equalsIgnoreCase("N/A")) {
                                    movie.setDirector(directorFromResponse);
                                    logger.info("Director for movie with id {} enriched with {}", movieId, movie.getDirector());
                                }

                                //Set year
                                if (movie.getYear() == null &&
                                        yearFromResponse != null &&
                                        !yearFromResponse.equalsIgnoreCase("N/A")) {
                                    movie.setYear(Year.parse(yearFromResponse));
                                    logger.info("Year for movie with id {} enriched with {}", movieId, movie.getYear());
                                }

                                movieRepository.save(movie);

                            },
                            () -> logger.warn("Movie with id {} was deleted before enrichment completed", movieId));
        } catch (RestClientException | NumberFormatException | DateTimeParseException e) {
            logger.error("Could not enrich movie with id {}: {}", movieId, e.getMessage());
        }
    }
}
