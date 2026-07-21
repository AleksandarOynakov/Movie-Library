package com.example.movielibrary.models.movie.movieDtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Year;

public class UpdateMovieDto {
    @NotBlank(message = "Title must not be blank")
    @Size(min = 4, max = 40, message = "Title must be min 4 and max 40 symbols")
    private String title;

    private String director;

    private Year year;

    private Double rating;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

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
