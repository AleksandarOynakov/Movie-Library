package com.example.movielibrary.helpers;

import com.example.movielibrary.models.movie.Movie;
import org.springframework.stereotype.Component;

import java.time.Year;

@Component
public class NullChecker {
    public boolean containsNullValue(Movie movie){
        return movie.getRating() == null || movie.getDirector() == null || movie.getYear() == null;
    }
}
