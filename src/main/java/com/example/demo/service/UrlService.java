package com.example.demo.service;

import com.example.demo.model.Url;
import com.example.demo.dto.UrlDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;


@Service
public interface UrlService {

    Url createUrlWithRandomSlug(UrlDto urlDto);
    Url persistShortLink(Url url);
    Url getEncodedUrl(String url);
    void deleteUrlByShortLink(String url);

    void deleteUrl(Url url);

    boolean isCustomSlugExists(String customSlug);

    Url createURLWithCustomSlug(UrlDto urlDto);

    Url getUrlByShortLink(String shortLink);
    Url setExpirationDate(String shortCode, LocalDateTime expirationDate);


}
