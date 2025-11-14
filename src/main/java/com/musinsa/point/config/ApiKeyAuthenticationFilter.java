package com.musinsa.point.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "X-API-KEY";

    private final PointProperties pointProperties;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!pointProperties.getSecurity().isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        SecurityContextHolder.clearContext();
        String apiKey = request.getHeader(HEADER_NAME);
        if (StringUtils.hasText(apiKey) && apiKey.equals(pointProperties.getSecurity().getApiKey())) {
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    "api-key-user",
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }
}
