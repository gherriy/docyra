package com.docyra.backend.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.docyra.backend.patient.InvalidCsvException;

public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCsvException.class)
    public ProblemDetail handleInvalidCsv(InvalidCsvException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

}
