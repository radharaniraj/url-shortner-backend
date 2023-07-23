package com.example.demo.service;

import com.example.demo.dto.UrlDto;
import com.example.demo.model.Url;
import com.example.demo.repository.UrlRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class UrlServiceImplTest {
    @Mock
    private UrlRepository urlRepository;

    private MockMvc mockMvc;

    @InjectMocks
    private UrlServiceImpl urlService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateUrlWithRandomSlug() {
        Url url = new Url();
        String originalUrl = "http://example.com";
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc1234");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(3));

        when(urlRepository.save(any(Url.class))).thenReturn(url);

        UrlDto urlDto = new UrlDto();
        urlDto.setUrl(originalUrl);
        urlDto.setExpirationDate("");

        Url result = urlService.createUrlWithRandomSlug(urlDto);

        assertEquals("http://example.com", result.getOriginalUrl());
        assertEquals(7, result.getShortLink().length());
        assertEquals(LocalDateTime.now().plusSeconds(60).getYear(), result.getExpirationDate().getYear());
    }

    @Test
    public void testCreateURLWithCustomSlug() {
        Url url = new Url();
        String originalUrl = "http://example.com";
        String customSlug = "abc1234";
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc1234");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(3));

        when(urlRepository.save(any(Url.class))).thenReturn(url);

        UrlDto urlDto = new UrlDto();
        urlDto.setUrl(originalUrl);
        urlDto.setCustomSlug(customSlug);
        urlDto.setExpirationDate("2023-11-11");

        Url result = urlService.createURLWithCustomSlug(urlDto);

        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(customSlug, result.getShortLink());
        assertEquals(LocalDateTime.now().plusSeconds(60).getYear(), result.getExpirationDate().getYear());
    }

    @Test
    public void testPersistShortLink() {
        Url url = new Url();
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc123");
        url.setCreationDate(LocalDateTime.now());

        when(urlRepository.save(Mockito.eq(url))).thenReturn(url);

        Url result = urlService.persistShortLink(url);

        assertNotNull(result);
        assertEquals(url.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(url.getShortLink(), result.getShortLink());
        assertEquals(url.getCreationDate(), result.getCreationDate());

        verify(urlRepository, times(1)).save(Mockito.eq(url));
    }
    @Test
    public void testDeleteShortLink() {
        Url url = new Url();
        url.setId(1);
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc123");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(7));

        urlService.deleteUrl(url);

        verify(urlRepository).delete(url);
    }

    @Test
    public void testIsCustomSlugExists() {
        when(urlRepository.existsByShortLink("custom-slug")).thenReturn(true);

        boolean result = urlService.isCustomSlugExists("custom-slug");

        assertTrue(result);
    }

    @Test
    public void testIsCustomSlugExistsWhenSlugDoesNotExist() {
        when(urlRepository.existsByShortLink("custom-slug")).thenReturn(false);

        boolean result = urlService.isCustomSlugExists("custom-slug");
        assertFalse(result);
    }

    @Test
    public void testGetEncodedUrl() {
        Url url = new Url();
        url.setId(1);
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc123");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(7));

        when(urlRepository.findByShortLink("abc123")).thenReturn(url);

        Url result = urlService.getEncodedUrl("abc123");

        assertNotNull(result);
        assertEquals("http://example.com", result.getOriginalUrl());
        assertEquals("abc123", result.getShortLink());

        verify(urlRepository).findByShortLink("abc123");
    }

    @Test
    public void testGetUrlByShortLink_ExistingShortLink() {
        // Prepare test data
        String shortLink = "abc";
        Url urlObj = new Url(); // Create a Url object as needed

        // Mock the urlRepository to return an existing Url object
        when(urlRepository.findByShortLink(shortLink)).thenReturn(urlObj);

        // Execute the service method
        Url resultUrl = urlService.getUrlByShortLink(shortLink);

        assertEquals(urlObj, resultUrl);

        verify(urlRepository, times(1)).findByShortLink(shortLink);
    }

    @Test
    public void testGetUrlByShortLink_NonExistingShortLink() {
        String shortLink = "abc";

        when(urlRepository.findByShortLink(shortLink)).thenReturn(null);

        Url resultUrl = urlService.getUrlByShortLink(shortLink);

        assertEquals(null, resultUrl);

        verify(urlRepository, times(1)).findByShortLink(shortLink);
    }




}

