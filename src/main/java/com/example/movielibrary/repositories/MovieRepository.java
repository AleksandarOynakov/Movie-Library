package com.example.movielibrary.repositories;

import com.example.movielibrary.models.movie.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovieRepository extends JpaRepository<Movie,Integer> {
}
