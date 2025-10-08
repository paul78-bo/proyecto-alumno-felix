package com.peral.alumnos.filter;

import com.peral.alumnos.config.JwtUtil;
import com.peral.alumnos.service.CustomUserDetailsService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        System.out.println("🔐 JwtFilter processing: " + request.getRequestURI());
        
        final String authorizationHeader = request.getHeader("Authorization");
        System.out.println("📨 Authorization Header: " + authorizationHeader);

        String username = null;
        String jwt = null;

        // Extraer token del header "Authorization: Bearer <token>"
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            System.out.println("📝 JWT Token: " + jwt);
            
            try {
                username = jwtUtil.extractUsername(jwt);
                System.out.println("👤 Extracted username: " + username);
            } catch (Exception e) {
                System.out.println("❌ Error extracting username: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ No Bearer token found");
        }

        // Validar token y configurar autenticación en Spring Security
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            System.out.println("✅ UserDetails loaded: " + userDetails.getUsername() + " with roles: " + userDetails.getAuthorities());
            
            if (jwtUtil.validateToken(jwt, userDetails)) {
                System.out.println("🔑 Token validated successfully");
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("🎯 SecurityContext set with authentication for: " + userDetails.getUsername());
            } else {
                System.out.println("❌ Token validation failed");
            }
        } else {
            System.out.println("⚠️ No username or already authenticated");
        }
        
        filterChain.doFilter(request, response);
    }
}