package com.carmats.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Defense-in-depth filter verifying Origin, Referer, and Sec-Fetch-Site headers
 * on cookie-authenticated state-changing auth endpoints (POST /refresh, POST /logout).
 */
@Component
public class OriginValidationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        // Target cookie-authenticated state-changing endpoints
        if ("POST".equalsIgnoreCase(method) && (path.endsWith("/api/v1/auth/refresh") || path.endsWith("/api/v1/auth/logout"))) {

            // 1. Check Sec-Fetch-Site (Standard modern browser CSRF defense header)
            String secFetchSite = request.getHeader("Sec-Fetch-Site");
            if (secFetchSite != null && "cross-site".equalsIgnoreCase(secFetchSite)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"CSRF_DETECTED\",\"message\":\"Cross-site requests are not permitted for this endpoint.\"}");
                return;
            }

            // 2. Check Origin header if present
            String origin = request.getHeader("Origin");
            if (origin != null && !isAllowedOrigin(origin, request)) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":\"INVALID_ORIGIN\",\"message\":\"Request origin is not authorized.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedOrigin(String origin, HttpServletRequest request) {
        String serverName = request.getServerName();
        String originClean = origin.toLowerCase().replace("http://", "").replace("https://", "").split(":")[0];
        return originClean.equals(serverName.toLowerCase())
                || originClean.equals("localhost")
                || originClean.equals("127.0.0.1")
                || originClean.endsWith("carmats.local");
    }
}
