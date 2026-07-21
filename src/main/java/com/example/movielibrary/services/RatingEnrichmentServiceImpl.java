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

@Service
public class RatingEnrichmentServiceImpl implements RatingEnrichmentService {
    private static final Logger logger = LoggerFactory.getLogger(RatingEnrichmentServiceImpl.class);
    private final OmdbClient omdbClient;
    private final MovieRepository movieRepository;

    public RatingEnrichmentServiceImpl(OmdbClient omdbClient, MovieRepository movieRepository) {
        this.omdbClient = omdbClient;
        this.movieRepository = movieRepository;
    }

    @Async
    @Override
    public void enrichRating(int movieId, String title, Year year) {
        try {
            OmdbResponseDto responseDto = omdbClient.findMovie(title, year);
            if (responseDto == null || !responseDto.wasFound()) {
                logger.warn("OMDb didn't find movie {} ({}), error: {}", title, year, responseDto.error());
                return;
            }

            String rating = responseDto.imdbRating();
            String director = responseDto.director();
            String yearFromResponse = responseDto.year();

            if (rating == null || rating.equalsIgnoreCase("N/A")) {
                logger.warn("OMDb didn't return rating for movie {} ({})", title, year);
                return;
            }

            double ratingDouble = Double.parseDouble(rating);

            movieRepository.findById(movieId)
                    .ifPresentOrElse(movie -> {
                                if (movie.getRating() == null) {
                                    movie.setRating(ratingDouble);
                                    logger.info("Rating for movie {} enriched with {}", movieId, ratingDouble);
                                }
                                if (movie.getDirector() == null && !director.equalsIgnoreCase("N/A")) {
                                    movie.setDirector(director);
                                    logger.info("Director for movie {} enriched with {}", movieId, director);
                                }
                                if (movie.getYear() == null && !yearFromResponse.equalsIgnoreCase("N/A")) {
                                    movie.setYear(Year.parse(yearFromResponse));
                                    logger.info("Year for movie {} enriched with {}", movieId, yearFromResponse);
                                }
                                movieRepository.save(movie);

                            },
                            () -> logger.warn("Movie {} was deleted before enrichment completed", movieId));
        } catch (RestClientException | NumberFormatException e) {
            logger.error("Could not enrich rating for movie {}: {}", movieId, e.getMessage());
        }
    }
}
