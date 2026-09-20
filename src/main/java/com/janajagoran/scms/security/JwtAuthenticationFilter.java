package com.janajagoran.scms.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;
    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);
        try {
            final String email = jwtUtil.extractEmail(token);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails rawDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(token, email) && rawDetails.isEnabled()) {
                    UserPrincipal userDetails = (UserPrincipal) rawDetails;

                    // Force any pending "must change password" (temp password from Admin, first login,
                    // etc.) through before letting the token touch anything else in the app.
                    if (userDetails.isMustChangePassword() && !isPasswordChangeExempt(request)) {
                        sendMustChangePasswordResponse(response, request);
                        return;
                    }

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception ex) {
            // Invalid token - leave request unauthenticated; entry point will handle 401
        }

        filterChain.doFilter(request, response);
    }

    /** Routes a user with a pending forced password-change is still allowed to hit. */
    private boolean isPasswordChangeExempt(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/")
                || (path.equals("/api/profile/me") && "GET".equalsIgnoreCase(request.getMethod()));
    }

    private void sendMustChangePasswordResponse(HttpServletResponse response, HttpServletRequest request) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(423); // 423 Locked -- distinct from a plain 401/403 so the frontend can redirect precisely

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 423);
        body.put("error", "PasswordChangeRequired");
        body.put("message", "You must set a new password before continuing. Use POST /api/auth/first-login-password.");
        body.put("path", request.getRequestURI());

        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
