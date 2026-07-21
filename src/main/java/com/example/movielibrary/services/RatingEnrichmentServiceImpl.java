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
                logger.warn("OMDb didn't find movie {} ({})", title, year);
                return;
            }

            String rating = responseDto.imdbRating();
            if (rating == null || rating.equalsIgnoreCase("N/A")) {
                logger.warn("OMDb didn't return rating for movie {} ({})", title, year);
                return;
            }

            double ratingDouble = Double.parseDouble(rating);

            movieRepository.findById(movieId)
                    .ifPresentOrElse(movie -> {
                                movie.setRating(ratingDouble);
                                movieRepository.save(movie);
                                logger.info("Rating for movie {} enriched with {}", movieId, ratingDouble);
                            },
                            () -> logger.warn("Movie {} was deleted before enrichment completed", movieId));
        } catch (RestClientException | NumberFormatException e) {
            logger.error("Could not enrich rating for movie {}: {}", movieId, e.getMessage());
        }
    }
}
