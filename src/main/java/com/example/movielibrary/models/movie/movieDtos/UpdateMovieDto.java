package com.example.movielibrary.models.movie.movieDtos;

import java.time.Year;

public class UpdateMovieDto {

    private String director;

    private Year year;

    private Double rating;

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public Year getYear() {
        return year;
    }

    public void setYear(Year year) {
        this.year = year;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
