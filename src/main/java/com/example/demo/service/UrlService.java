package com.example.demo.service;

import com.example.demo.model.Url;
import com.example.demo.dto.UrlDto;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public interface UrlService {
    public CompletableFuture<Url> createUrlWithRandomSlug(UrlDto urlDto);
    public Url persistShortLink(Url url);
    public Url getEncodedUrl(String url);
    public void deleteShortLink(Url url);

    public boolean isCustomSlugExists(String customSlug);

    public CompletableFuture<Url> createURLWithCustomSlug(UrlDto urlDto);
}
