package com.example.demo.middleware;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 40;
    private static final Map<String, Long> requestCounts = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String clientIP = getClientIP(request);
        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Incoming request - URL: {}, Method: {}, Headers: {}", request.getRequestURI(), request.getMethod(), request.getHeaderNames());

        // Check if the client IP is already present in the requestCounts map
        if (requestCounts.containsKey(clientIP)) {
            long requestCount = requestCounts.get(clientIP);

            // Check if the client has exceeded the maximum requests per minute
            if (requestCount >= MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many requests");
                return false; // Stop the request from proceeding to the controller
            }

            // Increment the request count for the client IP
            requestCounts.put(clientIP, requestCount + 1);
        } else {
            // Add the client IP to the requestCounts map with an initial count of 1
            requestCounts.put(clientIP, 1L);
        }

        // Continue with the request handling
        return true;
    }

    private String getClientIP(HttpServletRequest request) {
        String clientIP = request.getHeader("X-Forwarded-For");
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("Proxy-Client-IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("HTTP_CLIENT_IP");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (clientIP == null || clientIP.isEmpty() || "unknown".equalsIgnoreCase(clientIP)) {
            clientIP = request.getRemoteAddr();
        }
        return clientIP;
    }
}

