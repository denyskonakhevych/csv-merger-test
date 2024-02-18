package com.server.controller;

import com.server.exception.InvalidCsvException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = {InvalidCsvException.class})
    protected ResponseStatusException handleDeviceFingerprintEntryNotFound(final InvalidCsvException e) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(value = {Exception.class})
    protected ResponseStatusException handleException(final Exception e) {
        return new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
    }
}
