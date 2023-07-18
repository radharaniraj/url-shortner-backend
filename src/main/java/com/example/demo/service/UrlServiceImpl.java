package com.example.demo.service;

import com.example.demo.model.Url;
import com.example.demo.dto.UrlDto;
import com.example.demo.repository.UrlRepository;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import org.apache.commons.lang3.StringUtils;


import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Component
public class UrlServiceImpl implements UrlService{

    final private UrlRepository urlRepository;
    @Autowired
    public UrlServiceImpl(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @Override
    @Async
    public Url createUrlWithRandomSlug(UrlDto urlDto) {
        String slug = generateRandomSlug();
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = getExpirationDate(urlDto.getExpirationDate(), creationDate);

        Url urlToPersist = new Url(urlDto.getUrl(), slug, creationDate, expirationDate);
        return persistShortLink(urlToPersist);
    }

    @Async
    @Override
    public Url createURLWithCustomSlug(UrlDto urlDto){
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = getExpirationDate(urlDto.getExpirationDate(), creationDate);

        Url urlToPersist = new Url(urlDto.getUrl(), urlDto.getCustomSlug(), creationDate, expirationDate);
        return persistShortLink(urlToPersist);
    }

    private LocalDateTime getExpirationDate(String expirationDate, LocalDateTime creationDate) {
        if(StringUtils.isBlank(expirationDate)){
            return creationDate.plusSeconds(60);
        }
        LocalDate expirationDateObj = LocalDate.parse(expirationDate, DateTimeFormatter.ISO_DATE);
        LocalDateTime expirationDateTime = expirationDateObj.atStartOfDay();
        return expirationDateTime;
    }

    private String encodeUrl(String url) {
        String encodedUrl="";
        LocalDateTime time=LocalDateTime.now();
        encodedUrl= Hashing.murmur3_32_fixed().hashString(url.concat(time.toString()), StandardCharsets.UTF_8).toString();
        return encodedUrl;
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
        Url urlToRet=urlRepository.save(url);
        return urlToRet;
    }

    @Override
    public Url getEncodedUrl(String url) {
       Url urlToRet=urlRepository.findByShortLink(url);
       return urlToRet;
    }

    @Override
    public void deleteShortLink(Url url) {
        urlRepository.delete(url);
    }

    @Override
    public boolean isCustomSlugExists(String customSlug) {
        return urlRepository.existsByShortLink(customSlug);
    }
}
