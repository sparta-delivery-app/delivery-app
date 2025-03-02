package com.example.deliveryapp.config;

import com.example.deliveryapp.domain.common.exception.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtFilter implements Filter {

    private final JwtUtil jwtUtil;
    private static final String AUTHORIZATION = "Authorization";
    private static final List<String> PERMIT_URIS = List.of("/users/signin", "/users/signup");

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws ServletException, IOException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String url = request.getRequestURI();

        if (PERMIT_URIS.contains(url)) {
            chain.doFilter(servletRequest, servletResponse);
            return;
        }

        String bearerJwt = request.getHeader(AUTHORIZATION);

        if (bearerJwt == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "JWT 토큰이 필요합니다.");
            return;
        }

        String jwt = jwtUtil.substringToken(bearerJwt);

        try {
            Claims claims = jwtUtil.extractClaims(jwt);

            if (claims == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 JWT 토큰입니다.");
                return;
            }

            request.setAttribute("userId", Long.parseLong(claims.getSubject()));
            request.setAttribute("email", claims.get("email"));
            request.setAttribute("name", claims.get("name"));
            request.setAttribute("userRole", claims.get("userRole"));

            chain.doFilter(servletRequest, servletResponse);
        } catch (SecurityException | MalformedJwtException e) {
            log.error(ErrorCode.INVALID_SIGNATURE.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.INVALID_SIGNATURE.getMessage());
        } catch (ExpiredJwtException e) {
            log.error(ErrorCode.EXPIRED_TOKEN.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorCode.EXPIRED_TOKEN.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error(ErrorCode.UNSUPPORTED_TOKEN.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorCode.UNSUPPORTED_TOKEN.getMessage());
        } catch (IllegalArgumentException e) {
            log.error(ErrorCode.EMPTY_CLAIMS.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorCode.EMPTY_CLAIMS.getMessage());
        } catch (Exception e) {
            log.error(ErrorCode.INVALID_TOKEN.getMessage(), e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorCode.INVALID_TOKEN.getMessage());
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
