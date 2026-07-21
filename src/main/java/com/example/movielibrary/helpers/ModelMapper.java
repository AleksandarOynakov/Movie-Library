package com.example.movielibrary.helpers;

import com.example.movielibrary.models.movie.Movie;
import com.example.movielibrary.models.movie.movieDtos.CreateMovieDto;
import com.example.movielibrary.models.movie.movieDtos.UpdateMovieDto;
import com.example.movielibrary.models.user.userDtos.RegisterUserDto;
import com.example.movielibrary.models.user.Role;
import com.example.movielibrary.models.user.ApplicationUser;
import org.springframework.stereotype.Component;

@Component
public class ModelMapper {
    public ApplicationUser formDtoToObject(RegisterUserDto registerUserDto){
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername(registerUserDto.getUsername());
        applicationUser.setPassword(registerUserDto.getPassword());
        applicationUser.setRole(Role.USER);
        return applicationUser;
    }

    public Movie fromDtoToObject(CreateMovieDto createMovieDto){
        Movie movie = new Movie();
        movie.setTitle(createMovieDto.getTitle());
        movie.setDirector(createMovieDto.getDirector());
        movie.setYear(createMovieDto.getYear());
        movie.setRating(createMovieDto.getRating());
        return movie;
    }

    public Movie fromDtoToObject(Movie movie, UpdateMovieDto updateMovieDto){

        if(updateMovieDto.getTitle() != null){
            movie.setTitle(updateMovieDto.getTitle());
        }

        if(updateMovieDto.getDirector() != null){
            movie.setDirector(updateMovieDto.getDirector());
        }

        if(updateMovieDto.getYear() != null){
            movie.setYear(updateMovieDto.getYear());
        }

        if(updateMovieDto.getRating() != null){
            movie.setRating(updateMovieDto.getRating());
        }
        return movie;
    }
}
