package com.example.demo.service;

import com.example.demo.model.Url;
import com.example.demo.model.UrlDto;
import org.springframework.stereotype.Service;

@Service
public interface UrlService {
    public Url generateShortLink(UrlDto urlDto);
    public Url persistShortLink(Url url);
    public Url getEncodedUrl(String url);
    public void deleteShortLink(Url url);

    public boolean isCustomSlugExists(String customSlug);

    public Url generateURLWithCustomSlug(UrlDto urlDto);
}
