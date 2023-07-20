package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.exceptions.CustomSlugExistsException;
import com.example.demo.exceptions.GlobalExceptionHandler;
import com.example.demo.exceptions.UrlProcessingException;
import com.example.demo.model.Url;
import com.example.demo.service.UrlService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
        // Arrange
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

        // Act
        ResponseEntity<?> responseEntity = urlShorteningController.generateShortLink(urlDto);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());

        UrlResponseDto responseDto = (UrlResponseDto) responseEntity.getBody();
        assertEquals(mockedUrl.getOriginalUrl(), responseDto.getOriginalUrl());
        assertEquals(mockedUrl.getShortLink(), responseDto.getShortLink());
    }

    @Test
    void testGenerateShortLink_WithCustomSlug_SlugExists() throws Exception, CustomSlugExistsException {
        // Arrange
        UrlDto urlDto = new UrlDto();
        urlDto.setCustomSlug("customS");
        urlDto.setUrl("https://www.google.com");

        when(urlService.isCustomSlugExists("customS")).thenReturn(true);

        // Act and Assert
        mockMvc.perform(post("/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(urlDto))) // Helper method to convert object to JSON string
                .andExpect(status().isConflict());

    }

    // Helper method to convert an object to JSON string
    private String asJsonString(Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateShortLink_WithoutCustomSlug_Success() {
        // Arrange
        UrlDto urlDto = new UrlDto();
        urlDto.setCustomSlug(null);

        when(urlService.createUrlWithRandomSlug(urlDto)).thenReturn(new Url(/* mock the required URL object */));

        // Act
        ResponseEntity<?> responseEntity = urlShorteningController.generateShortLink(urlDto);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        // Add further assertions based on the expected response
    }

    @Test
    void testGenerateShortLink_Error() {
        // Arrange
        UrlDto urlDto = new UrlDto();
        urlDto.setCustomSlug("customSlug");

        when(urlService.isCustomSlugExists("customSlug")).thenReturn(false);
        when(urlService.createURLWithCustomSlug(urlDto)).thenReturn(null);

        // Act and Assert
        UrlProcessingException exception = assertThrows(UrlProcessingException.class, () -> {
            urlShorteningController.generateShortLink(urlDto);
        });
        assertEquals("There was an error processing your request. Please try again.", exception.getMessage());
    }


    @Test
    public void testValidateCustomSlug_ExistingSlug_ConflictStatusReturned() {
        // Arrange
        String existingSlug = "existingSlug";
        ValidateCustomSlugRequestDto requestDto = new ValidateCustomSlugRequestDto(existingSlug);
        when(urlService.isCustomSlugExists(existingSlug)).thenReturn(true);

        // Act
        ResponseEntity<ValidateCustomSlugResponseDto> response = urlShorteningController.validateCustomSlug(requestDto);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Custom slug already exists", response.getBody().getMessage());
        verify(urlService, times(1)).isCustomSlugExists(existingSlug);
    }

    @Test
    public void testValidateCustomSlug_AvailableSlug_OKStatusReturned() {
        // Arrange
        String availableSlug = "availableSlug";
        ValidateCustomSlugRequestDto requestDto = new ValidateCustomSlugRequestDto(availableSlug);
        when(urlService.isCustomSlugExists(availableSlug)).thenReturn(false);

        // Act
        ResponseEntity<ValidateCustomSlugResponseDto> response = urlShorteningController.validateCustomSlug(requestDto);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Custom slug is available", response.getBody().getMessage());
        verify(urlService, times(1)).isCustomSlugExists(availableSlug);
    }


    @Test
    public void testRedirectToOriginalUrl_InvalidShortLink_BadRequestReturned() throws IOException {
        // Arrange
        String invalidShortLink = null;

        // Act
        ResponseEntity<?> response = urlShorteningController.redirectToOriginalUrl(invalidShortLink, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponseDto);
        ErrorResponseDto errorResponseDto = (ErrorResponseDto) response.getBody();
        assertEquals("400", errorResponseDto.getStatus());
        assertEquals("Invalid URL", errorResponseDto.getError());
    }

    @Test
    public void testRedirectToOriginalUrl_NonExistingShortLink_BadRequestReturned() throws IOException {
        // Arrange
        String nonExistingShortLink = "nonExistingShortLink";
        when(urlService.getEncodedUrl(nonExistingShortLink)).thenReturn(null);

        // Act
        ResponseEntity<?> response = urlShorteningController.redirectToOriginalUrl(nonExistingShortLink, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponseDto);
        ErrorResponseDto errorResponseDto = (ErrorResponseDto) response.getBody();
        assertEquals("400", errorResponseDto.getStatus());
        assertEquals("URL does not exist or it might have expired!", errorResponseDto.getError());
        verify(urlService, times(1)).getEncodedUrl(nonExistingShortLink);
    }

    @Test
    public void testRedirectToOriginalUrl_ExpiredUrl_OKStatusReturned() throws IOException {
        // Arrange
        String expiredShortLink = "expiredShortLink";
        Url expiredUrl = new Url();
        expiredUrl.setExpirationDate(LocalDateTime.now().minusDays(1));
        when(urlService.getEncodedUrl(expiredShortLink)).thenReturn(expiredUrl);

        // Act
        ResponseEntity<?> response = urlShorteningController.redirectToOriginalUrl(expiredShortLink, httpServletResponse);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponseDto);
        ErrorResponseDto errorResponseDto = (ErrorResponseDto) response.getBody();
        assertEquals("200", errorResponseDto.getStatus());
        assertEquals("URL expired. Please try generating a fresh one.", errorResponseDto.getError());
        verify(urlService, times(1)).getEncodedUrl(expiredShortLink);
        verify(urlService, times(1)).deleteShortLink(expiredUrl);
        verify(httpServletResponse, never()).sendRedirect(anyString());
    }

    @Test
    public void testRedirectToOriginalUrl_ValidUrl_RedirectPerformed() throws IOException {
        // Arrange
        String validShortLink = "validShortLink";
        Url validUrl = new Url();
        validUrl.setOriginalUrl("https://www.example.com");
        validUrl.setExpirationDate(LocalDateTime.now().plusDays(1));
        when(urlService.getEncodedUrl(validShortLink)).thenReturn(validUrl);

        // Act
        ResponseEntity<?> response =  urlShorteningController.redirectToOriginalUrl(validShortLink, httpServletResponse);

        // Assert
        assertNull(response);
        verify(urlService, times(1)).getEncodedUrl(validShortLink);
        verify(httpServletResponse, times(1)).sendRedirect(validUrl.getOriginalUrl());
    }
}
