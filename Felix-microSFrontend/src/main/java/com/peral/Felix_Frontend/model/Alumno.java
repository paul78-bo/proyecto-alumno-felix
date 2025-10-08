package com.peral.Felix_Frontend.model;

public class Alumno {
    private Long id;
    private String nombre;
    private String email;
    private Integer edad;
    private String carrera;
    
    // Constructores
    public Alumno() {}
    
    public Alumno(String nombre, String email, Integer edad, String carrera) {
        this.nombre = nombre;
        this.email = email;
        this.edad = edad;
        this.carrera = carrera;
    }
    
    // Getters y Setters
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