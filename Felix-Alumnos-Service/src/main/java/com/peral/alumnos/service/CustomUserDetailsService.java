package com.peral.alumnos.service;

import com.peral.alumnos.model.Usuario;
import com.peral.alumnos.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("🔍 Buscando usuario en BD: " + username);
        
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("❌ Usuario no encontrado: " + username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        System.out.println("✅ Usuario encontrado: " + usuario.getUsername());
        System.out.println("✅ Password: " + usuario.getPassword());
        System.out.println("✅ Rol: " + usuario.getRole());
        System.out.println("✅ Enabled: " + usuario.getEnabled());

        boolean enabled = usuario.getEnabled() != null ? usuario.getEnabled() : true;
        
        System.out.println("✅ Usuario habilitado: " + enabled);

        // ✅ SOLUCIÓN: No agregues "ROLE_" porque ya está en la BD
        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(usuario.getRole()))) // ← CAMBIO AQUÍ
                .disabled(!enabled)
                .build();
    }
}