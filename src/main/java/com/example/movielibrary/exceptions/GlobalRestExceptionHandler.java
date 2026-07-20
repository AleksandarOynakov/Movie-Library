package com.example.movielibrary.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalRestExceptionHandler {

    @ExceptionHandler
    public String duplicateEntityExceptionHandler(DuplicateEntityException e){
        throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage());
    }

}
