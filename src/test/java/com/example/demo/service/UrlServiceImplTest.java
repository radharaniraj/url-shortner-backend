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
        // Mock the repository behavior
        Url url = new Url();
        String originalUrl = "http://example.com";
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc1234");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(3));

        // Mock the repository behavior
        when(urlRepository.save(any(Url.class))).thenReturn(url);

        // Create a test URL DTO
        UrlDto urlDto = new UrlDto();
        urlDto.setUrl(originalUrl);
        urlDto.setExpirationDate("");

        // Call the service method
        Url result = urlService.createUrlWithRandomSlug(urlDto);

        // Assert the result
        assertEquals("http://example.com", result.getOriginalUrl());
        assertEquals(7, result.getShortLink().length());
        assertEquals(LocalDateTime.now().plusSeconds(60).getYear(), result.getExpirationDate().getYear());
    }

    @Test
    public void testCreateURLWithCustomSlug() {
        // Mock the repository behavior
        Url url = new Url();
        String originalUrl = "http://example.com";
        String customSlug = "abc1234";
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc1234");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(3));

        // Mock the repository behavior
        when(urlRepository.save(any(Url.class))).thenReturn(url);

        // Create a test URL DTO
        UrlDto urlDto = new UrlDto();
        urlDto.setUrl(originalUrl);
        urlDto.setCustomSlug(customSlug);
        urlDto.setExpirationDate("2023-11-11");

        // Call the service method
        Url result = urlService.createURLWithCustomSlug(urlDto);

        // Assert the result
        assertEquals(originalUrl, result.getOriginalUrl());
        assertEquals(customSlug, result.getShortLink());
        assertEquals(LocalDateTime.now().plusSeconds(60).getYear(), result.getExpirationDate().getYear());
    }

    @Test
    public void testPersistShortLink() {
        // Create a mock URL object
        Url url = new Url();
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc123");
        url.setCreationDate(LocalDateTime.now());

        // Mock the repository behavior
        when(urlRepository.save(Mockito.eq(url))).thenReturn(url);

        // Call the service method
        Url result = urlService.persistShortLink(url);

        // Assert the result
        assertNotNull(result);
        assertEquals(url.getOriginalUrl(), result.getOriginalUrl());
        assertEquals(url.getShortLink(), result.getShortLink());
        assertEquals(url.getCreationDate(), result.getCreationDate());

        // Verify that the repository save method was called
        verify(urlRepository, times(1)).save(Mockito.eq(url));
    }
    @Test
    public void testDeleteShortLink() {
        // Create a mock Url object
        Url url = new Url();
        url.setId(1);
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc123");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(7));

        // Call the method under test
        urlService.deleteShortLink(url);

        // Verify that the delete method of the repository is called with the correct Url object
        verify(urlRepository).delete(url);
    }

    @Test
    public void testIsCustomSlugExists() {
        // Mock the repository behavior
        when(urlRepository.existsByShortLink("custom-slug")).thenReturn(true);

        // Call the method under test
        boolean result = urlService.isCustomSlugExists("custom-slug");

        // Assert the result
        assertTrue(result);
    }

    @Test
    public void testIsCustomSlugExistsWhenSlugDoesNotExist() {
        // Mock the repository behavior
        when(urlRepository.existsByShortLink("custom-slug")).thenReturn(false);

        // Call the method under test
        boolean result = urlService.isCustomSlugExists("custom-slug");

        // Assert the result
        assertFalse(result);
    }

    @Test
    public void testGetEncodedUrl() {
        // Create a mock Url object
        Url url = new Url();
        url.setId(1);
        url.setOriginalUrl("http://example.com");
        url.setShortLink("abc123");
        url.setCreationDate(LocalDateTime.now());
        url.setExpirationDate(LocalDateTime.now().plusDays(7));

        // Mock the repository behavior
        when(urlRepository.findByShortLink("abc123")).thenReturn(url);

        // Call the method under test
        Url result = urlService.getEncodedUrl("abc123");

        // Assert the result
        assertNotNull(result);
        assertEquals("http://example.com", result.getOriginalUrl());
        assertEquals("abc123", result.getShortLink());

        // Verify that the findByShortLink method of the repository is called with the correct argument
        verify(urlRepository).findByShortLink("abc123");
    }


}

