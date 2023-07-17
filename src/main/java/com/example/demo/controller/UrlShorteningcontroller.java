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
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import com.example.demo.exceptions.CustomSlugExistsException;
import com.example.demo.exceptions.UrlProcessingException;

import java.io.IOException;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import jakarta.validation.Valid;


@RestController
public class UrlShorteningcontroller {
    @Autowired
    private UrlService urlService;
    Logger logger = LoggerFactory.getLogger(getClass());
    @PostMapping("/generate")
    @Async
    public ResponseEntity<?> generateShortLink(@Valid @RequestBody UrlDto urlDto)
    {
        logger.info("Request Body: inside controller");
        logger.info(urlDto.toString());
            if (urlService.isCustomSlugExists(urlDto.getCustomSlug())) {
                throw new CustomSlugExistsException("Custom slug already exists");
            }

            Url urlToRet;

            if (urlDto.getCustomSlug() == null) {
                urlToRet = urlService.createUrlWithRandomSlug(urlDto);
            } else {
                urlToRet = urlService.createURLWithCustomSlug(urlDto);
            }
            if (urlToRet != null) {
                UrlResponseDto urlResponseDto = new UrlResponseDto();
                urlResponseDto.setOriginalUrl(urlToRet.getOriginalUrl());
                urlResponseDto.setExpirationDate(urlToRet.getExpirationDate());
                urlResponseDto.setShortLink(urlToRet.getShortLink());
                return ResponseEntity.ok(urlResponseDto);
            } else {
                throw new UrlProcessingException("There was an error processing your request. Please try again.");
            }
    }

    @PostMapping("/validateSlug")
    @Async
    public ResponseEntity<ValidateCustomSlugResponseDto>validateCustomSlug(@Valid @RequestBody ValidateCustomSlugRequestDto requestDto) {
        String customSlug = requestDto.getCustomSlug();

        if (urlService.isCustomSlugExists(customSlug)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ValidateCustomSlugResponseDto("Custom slug already exists"));
        } else {
            return ResponseEntity.ok(new ValidateCustomSlugResponseDto("Custom slug is available"));
        }
    }


    @Async
    @GetMapping("/{shortLink}")
    public ResponseEntity<?> redirectToOriginalUrl(@PathVariable String shortLink, HttpServletResponse response) throws IOException {
        if (StringUtils.isEmpty(shortLink)) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("400","Invalid URL"));
        }

        Url urlToRet = urlService.getEncodedUrl(shortLink);

        if (urlToRet == null) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponseDto("400","URL does not exist or it might have expired!"));

        }

        if (urlToRet.getExpirationDate().isBefore(LocalDateTime.now())) {
            urlService.deleteShortLink(urlToRet);
            return ResponseEntity.ok()
                    .body(new ErrorResponseDto("200", "URL expired. Please try generating a fresh one."));
        }

        response.sendRedirect(urlToRet.getOriginalUrl());
        return null;
    }
}
