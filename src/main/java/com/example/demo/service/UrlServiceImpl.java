package com.example.demo.service;

import com.example.demo.model.Url;
import com.example.demo.model.UrlDto;
import com.example.demo.repository.UrlRepository;
import com.google.common.hash.Hashing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.commons.lang3.StringUtils;


import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Component
public class UrlServiceImpl implements UrlService{
    @Autowired
    private UrlRepository urlRepository;

    @Override
    public Url generateShortLink(UrlDto urlDto) {
        String encodedUrl = encodeUrl(urlDto.getUrl());
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = getExpirationDate(urlDto.getExpirationDate(), creationDate);

        Url urlToPersist = new Url(urlDto.getUrl(), encodedUrl, creationDate, expirationDate);
        return persistShortLink(urlToPersist);
    }

    @Override
    public Url generateURLWithCustomSlug(UrlDto urlDto){
        LocalDateTime creationDate = LocalDateTime.now();
        LocalDateTime expirationDate = getExpirationDate(urlDto.getExpirationDate(), creationDate);

        Url urlToPersist = new Url(urlDto.getUrl(), urlDto.getCustomSlug(), creationDate, expirationDate);
        return persistShortLink(urlToPersist);
    }

    private LocalDateTime getExpirationDate(String expirationDate, LocalDateTime creationDate) {
        if(StringUtils.isBlank(expirationDate)){
            return creationDate.plusSeconds(60);
        }
        LocalDateTime expirationDateToRet= LocalDateTime.parse(expirationDate);
        return expirationDateToRet;
    }

    private String encodeUrl(String url) {
        String encodedUrl="";
        LocalDateTime time=LocalDateTime.now();
        encodedUrl= Hashing.murmur3_32_fixed().hashString(url.concat(time.toString()), StandardCharsets.UTF_8).toString();
        return encodedUrl;
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
