package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.exceptions.CustomSlugExistsException;
import com.example.demo.exceptions.GlobalExceptionHandler;
import com.example.demo.exceptions.UrlProcessingException;
import com.example.demo.model.Url;
import com.example.demo.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UrlShorteningcontrollerTest {

    @Mock
    private UrlService urlService;

    @Mock
    private HttpServletResponse httpServletResponse;


    @Autowired
    private UrlShorteningcontroller urlShorteningController;

    private MockMvc mockMvc;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        urlShorteningController = new UrlShorteningcontroller(urlService);
        mockMvc = MockMvcBuilders.standaloneSetup(urlShorteningController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testGenerateShortLink_WithCustomSlug_Success() throws Exception {
        String originalUrl = "https://www.google.com";
        String customSlug = "abcdef";
        String expirationDate = "2023-10-10";
        UrlDto urlDto = new UrlDto();
        urlDto.setCustomSlug(customSlug);
        urlDto.setUrl(originalUrl);
        urlDto.setExpirationDate(expirationDate);
        Url mockedUrl = new Url();
        mockedUrl.setOriginalUrl(originalUrl);
        mockedUrl.setShortLink(customSlug);
        when(urlService.createURLWithCustomSlug(urlDto)).thenReturn(mockedUrl);

        ResponseEntity<?> responseEntity = urlShorteningController.generateShortLink(urlDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        UrlResponseDto responseDto = (UrlResponseDto) responseEntity.getBody();
        assertEquals(mockedUrl.getOriginalUrl(), responseDto.getOriginalUrl());
        assertEquals(mockedUrl.getShortLink(), responseDto.getShortLink());
    }

    @Test
    void testGenerateShortLink_WithCustomSlug_SlugExists() throws Exception, CustomSlugExistsException {
        UrlDto urlDto = new UrlDto();
        urlDto.setCustomSlug("customS");
        urlDto.setUrl("https://www.google.com");

        when(urlService.isCustomSlugExists("customS")).thenReturn(true);

        mockMvc.perform(post("/generate/shortlink")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(urlDto)))
                .andExpect(status().isConflict());

    }

    private String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateShortLink_WithoutCustomSlug_Success() {
        UrlDto urlDto = new UrlDto();
        urlDto.setCustomSlug(null);

        when(urlService.createUrlWithRandomSlug(urlDto)).thenReturn(new Url(/* mock the required URL object */));

        ResponseEntity<?> responseEntity = urlShorteningController.generateShortLink(urlDto);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
    }

    @Test
    void testGenerateShortLink_Error() {
        UrlDto urlDto = new UrlDto();
        urlDto.setCustomSlug("customSlug");

        when(urlService.isCustomSlugExists("customSlug")).thenReturn(false);
        when(urlService.createURLWithCustomSlug(urlDto)).thenReturn(null);
        UrlProcessingException exception = assertThrows(UrlProcessingException.class, () -> {
            urlShorteningController.generateShortLink(urlDto);
        });
        assertEquals("There was an error processing your request. Please try again.", exception.getMessage());
    }


    @Test
    public void testValidateCustomSlug_ExistingSlug_ConflictStatusReturned() {
        String existingSlug = "existingSlug";
        ValidateCustomSlugRequestDto requestDto = new ValidateCustomSlugRequestDto(existingSlug);
        when(urlService.isCustomSlugExists(existingSlug)).thenReturn(true);
        ResponseEntity<ValidateCustomSlugResponseDto> response = urlShorteningController.validateCustomSlug(requestDto);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Custom slug already exists", response.getBody().getMessage());
        verify(urlService, times(1)).isCustomSlugExists(existingSlug);
    }

    @Test
    public void testValidateCustomSlug_AvailableSlug_OKStatusReturned() {
        String availableSlug = "availableSlug";
        ValidateCustomSlugRequestDto requestDto = new ValidateCustomSlugRequestDto(availableSlug);
        when(urlService.isCustomSlugExists(availableSlug)).thenReturn(false);
        ResponseEntity<ValidateCustomSlugResponseDto> response = urlShorteningController.validateCustomSlug(requestDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Custom slug is available", response.getBody().getMessage());
        verify(urlService, times(1)).isCustomSlugExists(availableSlug);
    }


    @Test
    public void testRedirectToOriginalUrl_NonExistingShortLink_BadRequestReturned() throws IOException {
        String nonExistingShortLink = "nonExistingShortLink";
        when(urlService.getEncodedUrl(nonExistingShortLink)).thenReturn(null);
        ResponseEntity<?> response = urlShorteningController.redirectToOriginalUrl(nonExistingShortLink, httpServletResponse);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponseDto);
        ErrorResponseDto errorResponseDto = (ErrorResponseDto) response.getBody();
        assertEquals("404", errorResponseDto.getStatus());
        assertEquals("URL does not exist or it might have expired!", errorResponseDto.getError());
        verify(urlService, times(1)).getEncodedUrl(nonExistingShortLink);
    }

    @Test
    public void testRedirectToOriginalUrl_ExpiredUrl_OKStatusReturned() throws IOException {
        String expiredShortLink = "expiredShortLink";
        Url expiredUrl = new Url();
        expiredUrl.setExpirationDate(LocalDateTime.now().minusDays(1));
        when(urlService.getEncodedUrl(expiredShortLink)).thenReturn(expiredUrl);
        ResponseEntity<?> response = urlShorteningController.redirectToOriginalUrl(expiredShortLink, httpServletResponse);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponseDto);
        ErrorResponseDto errorResponseDto = (ErrorResponseDto) response.getBody();
        assertEquals("400", errorResponseDto.getStatus());
        assertEquals("URL expired. Please try generating a fresh one.", errorResponseDto.getError());
        verify(urlService, times(1)).getEncodedUrl(expiredShortLink);
        verify(urlService, times(1)).deleteUrl(expiredUrl);
        verify(httpServletResponse, never()).sendRedirect(anyString());
    }

    @Test
    public void testRedirectToOriginalUrl_ValidUrl_RedirectPerformed() throws IOException {
        String validShortLink = "validShortLink";
        Url validUrl = new Url();
        validUrl.setOriginalUrl("https://www.example.com");
        validUrl.setExpirationDate(LocalDateTime.now().plusDays(1));
        when(urlService.getEncodedUrl(validShortLink)).thenReturn(validUrl);
        ResponseEntity<?> response = urlShorteningController.redirectToOriginalUrl(validShortLink, httpServletResponse);
        assertNull(response);
        verify(urlService, times(1)).getEncodedUrl(validShortLink);
        verify(httpServletResponse, times(1)).sendRedirect(validUrl.getOriginalUrl());
    }

    @Test
    public void testSetExpirationDate_ValidUrl() {
        String shortCode = "abc";
        String expirationDate = "2023-12-31";
        UrlExpireDto urlExpireDto = new UrlExpireDto();
        urlExpireDto.setExpirationDate(expirationDate);

        when(urlService.isCustomSlugExists(shortCode)).thenReturn(true);

        Url urlToRet = new Url(); // Create a Url object as needed
        when(urlService.setExpirationDate(eq(shortCode), any(LocalDateTime.class))).thenReturn(urlToRet);

        ResponseEntity<?> responseEntity = urlShorteningController.setExpirationDate(shortCode, urlExpireDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

    }

    @Test
    public void testSetExpirationDate_InvalidUrl() {
        String shortCode = "abc";
        String expirationDate = "2023-12-31";
        UrlExpireDto urlExpireDto = new UrlExpireDto();
        urlExpireDto.setExpirationDate(expirationDate);

        when(urlService.isCustomSlugExists(shortCode)).thenReturn(false);

        ResponseEntity<?> responseEntity = urlShorteningController.setExpirationDate(shortCode, urlExpireDto);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ErrorResponseDto errorResponse = (ErrorResponseDto) responseEntity.getBody();
        assertEquals("404", errorResponse.getStatus());
        assertEquals("URL does not exist or it might have expired!", errorResponse.getError());
    }

    @Test
    public void testDeleteShortUrl_ExistingShortLink() throws IOException {
        String shortLink = "abc";
        Url urlToDelete = new Url(); // Create a Url object as needed

        when(urlService.getUrlByShortLink(shortLink)).thenReturn(urlToDelete);

        ResponseEntity<?> responseEntity = urlShorteningController.deleteShortUrl(shortLink, null);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        DeleteUrlResponseDto deleteUrlResponseDto = (DeleteUrlResponseDto) responseEntity.getBody();
        assertEquals("Short URL has been successfully deleted.", deleteUrlResponseDto.getMessage());

        verify(urlService, times(1)).deleteUrlByShortLink(shortLink);
    }

    @Test
    public void testDeleteShortUrl_NonExistingShortLink() throws IOException {
        String shortLink = "abc";

        when(urlService.getUrlByShortLink(shortLink)).thenReturn(null);

        ResponseEntity<?> responseEntity = urlShorteningController.deleteShortUrl(shortLink, null);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        ErrorResponseDto errorResponse = (ErrorResponseDto) responseEntity.getBody();
        assertEquals("404", errorResponse.getStatus());
        assertEquals("URL does not exist or it might have expired!", errorResponse.getError());

        verify(urlService, never()).deleteUrlByShortLink(shortLink);
    }

}
