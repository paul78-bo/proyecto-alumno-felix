package com.peral.alumnos.controller;

import com.peral.alumnos.model.Alumno;

import com.peral.alumnos.repository.AlumnoRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/alumnos")
public class AlumnoController {
    
    private final AlumnoRepository repository;
    
    public AlumnoController(AlumnoRepository repository) {
        this.repository = repository;
    }
    
    // GET - Todos los alumnos (requiere autenticación)
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROFESOR', 'ROLE_ALUMNO')") // ← CAMBIO
    public List<Alumno> getAllAlumnos() {
        return repository.findAll();
    }
    
    // GET - Alumno por ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROFESOR', 'ROLE_ALUMNO')") // ← CAMBIO
    public Alumno getAlumnoById(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROFESOR')")
    public Alumno createAlumno(@RequestBody Alumno alumno) {
        System.out.println("➕ POST /alumnos - Creando alumno en BACKEND");
        System.out.println("📝 Datos recibidos en backend:");
        System.out.println("📝 Nombre: " + alumno.getNombre());
        System.out.println("📝 Email: " + alumno.getEmail());
        System.out.println("📝 Edad: " + alumno.getEdad());
        System.out.println("📝 Carrera: " + alumno.getCarrera());
        
        // Verificar autenticación
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("🔐 Usuario autenticado: " + auth.getName());
        System.out.println("🎯 Roles: " + auth.getAuthorities());
        
        try {
            Alumno savedAlumno = repository.save(alumno);
            System.out.println("✅ Alumno guardado en BD con ID: " + savedAlumno.getId());
            return savedAlumno;
        } catch (Exception e) {
            System.out.println("❌ Error guardando en BD: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
    // PUT - Actualizar alumno (solo admin y profesor)
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROFESOR')") // ← CAMBIO
    public Alumno updateAlumno(@PathVariable Long id, @RequestBody Alumno alumnoDetails) {
        Alumno alumno = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Alumno no encontrado"));
        
        alumno.setNombre(alumnoDetails.getNombre());
        alumno.setEmail(alumnoDetails.getEmail());
        alumno.setEdad(alumnoDetails.getEdad());
        alumno.setCarrera(alumnoDetails.getCarrera());
        
        return repository.save(alumno);
    }
    
    // DELETE - Eliminar alumno (solo admin)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')") // ← CAMBIO
    public String deleteAlumno(@PathVariable Long id) {
        repository.deleteById(id);
        return "Alumno eliminado correctamente";
    }
    
    // GET - Alumnos por carrera
    @GetMapping("/carrera/{carrera}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_PROFESOR', 'ROLE_ALUMNO')") // ← CAMBIO
    public List<Alumno> getAlumnosByCarrera(@PathVariable String carrera) {
        return repository.findByCarrera(carrera);
    }
}