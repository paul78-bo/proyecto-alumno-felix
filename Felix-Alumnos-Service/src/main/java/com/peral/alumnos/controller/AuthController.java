package com.peral.alumnos.controller;

import com.peral.alumnos.config.JwtUtil;
import com.peral.alumnos.dto.LoginRequest;
import com.peral.alumnos.dto.LoginResponse;
import com.peral.alumnos.service.CustomUserDetailsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // ✅ AGREGAR ESTE IMPORT


@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")  // Para permitir requests del frontend
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthenticationManager authenticationManager, 
                         CustomUserDetailsService userDetailsService, 
                         JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("🔐 Intento de login para: " + loginRequest.getUsername());
        
        try {
            // Autenticar usuario
            System.out.println("🔐 Autenticando con AuthenticationManager...");
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );
            System.out.println("✅ Autenticación exitosa");
            
        } catch (BadCredentialsException e) {
            System.out.println("❌ BadCredentialsException: Credenciales incorrectas");
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, "Credenciales inválidas"));
        } catch (Exception e) {
            System.out.println("❌ Error en autenticación: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest()
                    .body(new LoginResponse(null, null, null, "Error: " + e.getMessage()));
        }

        // Cargar usuario y generar token
        System.out.println("🔐 Cargando UserDetails...");
        final UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
        
        System.out.println("🔐 Generando token JWT...");
        final String jwt = jwtUtil.generateToken(userDetails);
        
        // Extraer rol del usuario
        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(grantedAuthority -> {
                    String authority = grantedAuthority.getAuthority();
                    System.out.println("✅ Authority encontrada: " + authority);
                    return authority.replace("ROLE_", "");
                })
                .orElse("USER");

        System.out.println("✅ Token generado para: " + loginRequest.getUsername() + " con rol: " + role);
        System.out.println("✅ Token: " + jwt);
        
        return ResponseEntity.ok(new LoginResponse(jwt, loginRequest.getUsername(), role, "Login exitoso"));
    }
    // Endpoint para verificar token
    @PostMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateTokenStructure(token)) {
                String username = jwtUtil.extractUsername(token);
                return ResponseEntity.ok().body("Token válido para usuario: " + username);
            }
        }
        return ResponseEntity.badRequest().body("Token inválido");
    }
    
 // En AuthController.java - agregar este método temporal
    @GetMapping("/test-auth")
    public String testAuthentication() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        // Verificar que los hashes funcionan
        String adminHash = "$2a$10$dX4Q6v8b1tS3rF5hJ7n9qA2c4e6g8y0z1x3v5b7d9f1h2j4l6m8n0p2";
        boolean adminValid = encoder.matches("admin123", adminHash);
        
        String profesorHash = "$2a$10$eY5Q7w9c2uT4sG6iK8o0rB3d5f7h9j1l2m4n6p8r0t2u4w6y8z0a2";
        boolean profesorValid = encoder.matches("prof123", profesorHash);
        
        String alumnoHash = "$2a$10$fZ6R8x0d3vU5tH7jL9p1sC4e6g8i0k2l3m5o7q9s1u3w5y7z9a1c3";
        boolean alumnoValid = encoder.matches("alum123", alumnoHash);
        
        return "BCrypt Test:\n" +
               "admin123 válido: " + adminValid + "\n" +
               "prof123 válido: " + profesorValid + "\n" +
               "alum123 válido: " + alumnoValid + "\n" +
               "Todos válidos: " + (adminValid && profesorValid && alumnoValid);
    }
}