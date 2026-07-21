package com.example.movielibrary.services;

import org.springframework.scheduling.annotation.Async;

import java.time.Year;

public interface RatingEnrichmentService {
    @Async
    void enrichRating(int movieId, String title, Year year);
}
