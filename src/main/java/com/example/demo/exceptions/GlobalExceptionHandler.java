package com.example.demo.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.example.demo.model.UrlErrorResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomSlugExistsException.class)
    public ResponseEntity<UrlErrorResponseDto> handleCustomSlugExistsException(CustomSlugExistsException ex) {
        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
        urlErrorResponseDto.setStatus("409");
        urlErrorResponseDto.setError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(urlErrorResponseDto);
    }

    @ExceptionHandler(UrlProcessingException.class)
    public ResponseEntity<UrlErrorResponseDto> handleUrlProcessingException(UrlProcessingException ex) {
        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
        urlErrorResponseDto.setStatus("400");
        urlErrorResponseDto.setError(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlErrorResponseDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<UrlErrorResponseDto> handleException(Exception ex) {
        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
        urlErrorResponseDto.setStatus("500");
        urlErrorResponseDto.setError("An unexpected error occurred.");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(urlErrorResponseDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<UrlErrorResponseDto> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
        urlErrorResponseDto.setStatus("400");
        urlErrorResponseDto.setError(errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(urlErrorResponseDto);
    }
}
