package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.model.Url;
import com.example.demo.service.UrlService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.demo.exceptions.CustomSlugExistsException;
import com.example.demo.exceptions.UrlProcessingException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import org.slf4j.Logger;
import jakarta.validation.Valid;


@RestController
public class UrlShorteningcontroller {
    Logger logger = LoggerFactory.getLogger(getClass());

    final private UrlService urlService;

    @Autowired
    public UrlShorteningcontroller(UrlService urlService) {
        this.urlService = urlService;
    }

    @PostMapping("/generate/shortlink")
    public ResponseEntity<UrlResponseDto> generateShortLink(@Valid @RequestBody UrlDto urlDto)
    {
            logger.info(urlDto.toString());
            Url urlToRet;

            if (urlDto.getCustomSlug() == null) {
                urlToRet = urlService.createUrlWithRandomSlug(urlDto);
            } else {
                if (urlService.isCustomSlugExists(urlDto.getCustomSlug())) {
                    throw new CustomSlugExistsException("Custom slug already exists");
                }
                urlToRet = urlService.createURLWithCustomSlug(urlDto);
            }
            if (urlToRet != null) {
                UrlResponseDto urlResponseDto = new UrlResponseDto();
                urlResponseDto.setOriginalUrl(urlToRet.getOriginalUrl());
                urlResponseDto.setExpirationDate(urlToRet.getExpirationDate());
                urlResponseDto.setShortLink(urlToRet.getShortLink());
                return ResponseEntity.status(HttpStatus.CREATED).body(urlResponseDto);
            } else {
                throw new UrlProcessingException("There was an error processing your request. Please try again.");
            }
    }

    @PostMapping("/validateSlug")
    public ResponseEntity<ValidateCustomSlugResponseDto>validateCustomSlug(@Valid @RequestBody ValidateCustomSlugRequestDto requestDto) {
        String customSlug = requestDto.getCustomSlug();

        if (urlService.isCustomSlugExists(customSlug)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ValidateCustomSlugResponseDto("Custom slug already exists"));
        } else {
            return ResponseEntity.ok(new ValidateCustomSlugResponseDto("Custom slug is available"));
        }
    }


    @GetMapping("/{shortLink}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response) throws IOException {


        Url urlToRet = urlService.getEncodedUrl(shortLink);

        if (urlToRet == null) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("404","URL does not exist or it might have expired!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
        }

        if (urlToRet.getExpirationDate().isBefore(LocalDateTime.now())) {

            urlService.deleteUrl(urlToRet);
            return ResponseEntity.ok()
                    .body(new ErrorResponseDto("400", "URL expired. Please try generating a fresh one."));
        }

        response.sendRedirect(urlToRet.getOriginalUrl());
        return null;
    }
    @DeleteMapping("/{shortLink}")
    public ResponseEntity<?> deleteShortUrl(@PathVariable String shortLink,HttpServletResponse response)throws IOException {
        Url urlToDelete = urlService.getUrlByShortLink(shortLink);
        if (urlToDelete == null) {
            ErrorResponseDto errorResponse = new ErrorResponseDto("404","URL does not exist or it might have expired!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
        }
        urlService.deleteUrlByShortLink(shortLink);
        DeleteUrlResponseDto data = new DeleteUrlResponseDto("Short URL has been successfully deleted.");
        return ResponseEntity.ok(data);
    }

    @PutMapping("/url/{shortCode}/expirationDate")
    public ResponseEntity<?> setExpirationDate(@PathVariable String shortCode,
                                              @Valid @RequestBody UrlExpireDto urlExpireDto) {
        LocalDate expirationDateObj = LocalDate.parse(urlExpireDto.getExpirationDate(), DateTimeFormatter.ISO_DATE);
        LocalDateTime parsedExpirationDate = expirationDateObj.atStartOfDay();
        if (!urlService.isCustomSlugExists(shortCode)){
            ErrorResponseDto errorResponse = new ErrorResponseDto("404","URL does not exist or it might have expired!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(errorResponse);
        }
        Url urlToRet = urlService.setExpirationDate(shortCode, parsedExpirationDate);

        UrlResponseDto urlResponseDto = new UrlResponseDto();
        urlResponseDto.setOriginalUrl(urlToRet.getOriginalUrl());
        urlResponseDto.setExpirationDate(urlToRet.getExpirationDate());
        urlResponseDto.setShortLink(urlToRet.getShortLink());
        return ResponseEntity.ok(urlResponseDto);
    }
}
