package com.example.demo.Controller;

import com.example.demo.model.Url;
import com.example.demo.model.UrlDto;
import com.example.demo.model.UrlErrorResponseDto;
import com.example.demo.model.UrlResponseDto;
import com.example.demo.model.ErrorDetail;
import com.example.demo.service.UrlService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;
import java.time.LocalDateTime;
import jakarta.validation.Valid;


@RestController
public class UrlShorteningcontroller {
    @Autowired
    private UrlService urlService;

    @PostMapping("/generate")
    public ResponseEntity<?> generateShortLink(@Valid @RequestBody UrlDto urlDto)
    {
        try {
            if (urlService.isCustomSlugExists(urlDto.getCustomSlug())) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Custom slug already exists");
            }
            Url urlToRet = null;
            if(urlDto.getCustomSlug() == null) {
                urlToRet = urlService.generateShortLink(urlDto);
            }
            else{
                urlToRet = urlService.generateURLWithCustomSlug(urlDto);
            }

            if (urlToRet != null) {
                UrlResponseDto urlResponseDto = new UrlResponseDto();
                urlResponseDto.setOriginalUrl(urlToRet.getOriginalUrl());
                urlResponseDto.setExpirationDate(urlToRet.getExpirationDate());
                urlResponseDto.setShortLink(urlToRet.getShortLink());
                return ResponseEntity.ok(urlResponseDto);
            } else {
                UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
                urlErrorResponseDto.setStatus("404");
                urlErrorResponseDto.setError("There was an error processing your request. Please try again.");
                return ResponseEntity.ok(urlErrorResponseDto);
            }
        } catch (Exception e) {
            UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
            urlErrorResponseDto.setStatus("500");
            urlErrorResponseDto.setError("An unexpected error occurred.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(urlErrorResponseDto);
        }

    }

    @GetMapping("/{shortLink}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response) throws IOException, IOException {

        if(StringUtils.isEmpty(shortLink))
        {
            UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
            urlErrorResponseDto.setError("Invalid Url");
            urlErrorResponseDto.setStatus("400");
            return new ResponseEntity<UrlErrorResponseDto>(urlErrorResponseDto,HttpStatus.OK);
        }
        Url urlToRet = urlService.getEncodedUrl(shortLink);

        if(urlToRet == null)
        {
            UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
            urlErrorResponseDto.setError("Url does not exist or it might have expired!");
            urlErrorResponseDto.setStatus("400");
            return new ResponseEntity<UrlErrorResponseDto>(urlErrorResponseDto,HttpStatus.OK);
        }

        if(urlToRet.getExpirationDate().isBefore(LocalDateTime.now()))
        {
            urlService.deleteShortLink(urlToRet);
            UrlErrorResponseDto urlErrorResponseDto = new UrlErrorResponseDto();
            urlErrorResponseDto.setError("Url Expired. Please try generating a fresh one.");
            urlErrorResponseDto.setStatus("200");
            return new ResponseEntity<UrlErrorResponseDto>(urlErrorResponseDto,HttpStatus.OK);
        }

        response.sendRedirect(urlToRet.getOriginalUrl());
        return null;
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldError().getDefaultMessage();
        return ResponseEntity.badRequest().body(new ErrorDetail(errorMessage));
    }
}
