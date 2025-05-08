package com.example.trending.filters;

import com.example.trending.service.TokenService;
import com.example.trending.utils.JWTUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        log.info("[filter debugger] in the jwt filter");

        String path = request.getRequestURI();
        String method = request.getMethod();

        log.info("path: {} and method: {}", path, method);

        // 🛑 如果是預檢請求，直接放行（非常重要）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            logger.info("✅ 預檢請求，跳過 JWT 驗證");
            filterChain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("[filter debugger] 1");

        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        log.info("[filter debugger] 2");

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Authorization header missing");
            return;
        }

        String jwt = authHeader.substring(7);

        log.info("[filter debugger] 3, we have bear");

        try {
            // 1. 解析 JWT（會驗簽 + 過期判斷）
            Claims claims = jwtUtil.extractClaims(jwt);

            // 2. 檢查過期時間（可以多一層檢查）
            if (claims.getExpiration().before(new Date())) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token expired");
                return;
            }

            log.info("[filter debugger] 4, bear is valid, token not expired.");

            // 3. 從 claims 取出 user 資訊
            Long userId = Long.parseLong(claims.getSubject());
            String role = claims.get("role", String.class);

            // 4. 檢查是否有效（可選：Blacklist / Token table）
            if (!tokenService.isAccessTokenValid(jwt)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Token revoked or invalid");
                return;
            }

            // 5. 設定 SecurityContext
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            log.error("[debug] exception 5");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Token expired");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            log.error("[debug] exception 6");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("Invalid token");
            return;
        }

        // 6. 放行到下一層
        filterChain.doFilter(request, response);
    }
}

