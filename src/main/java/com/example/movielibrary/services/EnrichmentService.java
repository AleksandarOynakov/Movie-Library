package com.example.movielibrary.services;

import org.springframework.scheduling.annotation.Async;

import java.time.Year;

public interface EnrichmentService {
    @Async
    void enrichMovie(int movieId, String title, Year year);
}
