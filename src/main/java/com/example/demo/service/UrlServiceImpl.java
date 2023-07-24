package com.example.demo.service;

import com.example.demo.dto.UrlDto;
import com.example.demo.model.Url;
import com.example.demo.repository.UrlRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class UrlServiceImpl implements UrlService {
    Logger logger = LoggerFactory.getLogger(getClass());

    final private UrlRepository urlRepository;

    @Autowired
    public UrlServiceImpl(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Override
    public Url createURLWithCustomSlug(UrlDto urlDto) {
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = getExpirationDate(urlDto.getExpirationDate(), creationDate);
        Url urlToPersist = new Url(urlDto.getUrl(), urlDto.getCustomSlug(), creationDate, expirationDate);
        logger.info(urlToPersist.toString());
        return persistShortLink(urlToPersist);
    }

    @Override
    public Url createUrlWithRandomSlug(UrlDto urlDto) {
        String slug = generateRandomSlug();
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = getExpirationDate(urlDto.getExpirationDate(), creationDate);
        Url urlToPersist = new Url(urlDto.getUrl(), slug, creationDate, expirationDate);
        return persistShortLink(urlToPersist);
    }

    private LocalDateTime getExpirationDate(String expirationDate, LocalDateTime creationDate) {
        if (StringUtils.isBlank(expirationDate)) {
            return creationDate.plusSeconds(60);
        }
        LocalDate expirationDateObj = LocalDate.parse(expirationDate, DateTimeFormatter.ISO_DATE);
        LocalDateTime expirationDateTime = expirationDateObj.atStartOfDay();
        return expirationDateTime;
    }

    private String generateRandomSlug() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        String generatedString = "";

        boolean isUnique = false;
        while (!isUnique) {
            generatedString = generateRandomString(characters, 6);
            if (!isCustomSlugExists(generatedString)) {
                isUnique = true;
            }
        }

        return generatedString;
    }

    private String generateRandomString(String characters, int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb.toString();
    }


    @Override
    public Url persistShortLink(Url url) {
        Url urlToRet = urlRepository.save(url);
        return urlToRet;
    }

    @Override
    @Cacheable(key = "#url", cacheManager = "cacheManager", value = "myCache")
    public Url getEncodedUrl(String url) {
        Url urlToRet = urlRepository.findByShortLink(url);
        return urlToRet;
    }

    @Override
    @CacheEvict(value = "myCache", key = "#url", cacheManager = "cacheManager")
    public void deleteUrlByShortLink(String url) {
        Url urlToRet = urlRepository.findByShortLink(url);
        deleteUrl((urlToRet));
    }

    public void deleteUrl(Url url) {
        urlRepository.delete(url);
    }

    @Override
    public boolean isCustomSlugExists(String customSlug) {
        return urlRepository.existsByShortLink(customSlug);
    }

    @Override
    public Url getUrlByShortLink(String shortLink) {
        Url urlObj = urlRepository.findByShortLink(shortLink);
        return urlObj;
    }

    @Override
    public Url setExpirationDate(String shortCode, LocalDateTime expirationDate) {
        Url url = urlRepository.findByShortLink(shortCode);
        if (url != null) {
            url.setExpirationDate(expirationDate);
            return urlRepository.save(url);
        }
        return null;
    }
}
