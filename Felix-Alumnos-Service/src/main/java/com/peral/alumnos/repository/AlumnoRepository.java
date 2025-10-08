package com.peral.alumnos.repository;

import com.peral.alumnos.model.Alumno;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AlumnoRepository extends JpaRepository<Alumno, Long> {
    List<Alumno> findByCarrera(String carrera);
}