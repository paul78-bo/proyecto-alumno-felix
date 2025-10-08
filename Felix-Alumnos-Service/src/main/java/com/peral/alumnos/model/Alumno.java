package com.peral.alumnos.model;

import jakarta.persistence.*;

@Entity
@Table(name = "alumnos")  // ← Nombre específico de tabla
public class Alumno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← Auto-increment MySQL
    private Long id;
    
    @Column(name = "nombre", length = 100, nullable = false)
    private String nombre;
    
    @Column(name = "email", length = 150, unique = true)
    private String email;
    
    @Column(name = "edad")
    private Integer edad;
    
    @Column(name = "carrera", length = 100)
    private String carrera;
    
    // Constructores, getters y setters (igual que antes)
    public Alumno() {}
    
    public Alumno(String nombre, String email, Integer edad, String carrera) {
        this.nombre = nombre;
        this.email = email;
        this.edad = edad;
        this.carrera = carrera;
    }
    
    // Getters y Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getEdad() { return edad; }
    public void setEdad(Integer edad) { this.edad = edad; }
    public String getCarrera() { return carrera; }
    public void setCarrera(String carrera) { this.carrera = carrera; }
}