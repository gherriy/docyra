package com.docyra.backend.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;


import com.docyra.backend.document.InvalidImageException;
import com.docyra.backend.document.MissingPatientException;
import com.docyra.backend.ocr.OcrException;
import com.docyra.backend.patient.InvalidCsvException;
import com.docyra.backend.pdf.PdfGenerationException;
import com.docyra.backend.scan.PageDetectionException;

public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidCsvException.class)
    public ProblemDetail handleInvalidCsv(InvalidCsvException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

        @ExceptionHandler(InvalidImageException.class)
    public ProblemDetail handleInvalidImage(InvalidImageException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

        @ExceptionHandler(OcrException.class)
    public ProblemDetail handleOcr(OcrException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFound(NotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }
    @ExceptionHandler(MissingPatientException.class)
    public ProblemDetail handleMissingPatient(MissingPatientException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }
    @ExceptionHandler({PdfGenerationException.class, PageDetectionException.class})
    public ProblemDetail handleImageProcessing(RuntimeException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.getMessage());
    }

}
