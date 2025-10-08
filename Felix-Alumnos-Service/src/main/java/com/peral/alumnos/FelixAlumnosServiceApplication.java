package com.peral.alumnos;

import org.springframework.boot.SpringApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FelixAlumnosServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FelixAlumnosServiceApplication.class, args);}
		
		

		// En tu clase principal o cualquier @Service/@Component
		public void generateBCryptHashes() {
		    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		    
		    System.out.println("=== HASHS BCrypt REALES ===");
		    
		    String hashAdmin = encoder.encode("admin");
		    System.out.println("UPDATE usuarios SET password = '" + hashAdmin + "' WHERE username = 'admin';");
		    
		    String hashPassword = encoder.encode("password");
		    System.out.println("UPDATE usuarios SET password = '" + hashPassword + "' WHERE username = 'profesor';");
		    
		    String hash123456 = encoder.encode("123456");
		    System.out.println("UPDATE usuarios SET password = '" + hash123456 + "' WHERE username = 'alumno';");
		    
		    // Verificación
		    System.out.println("\n=== VERIFICACIÓN ===");
		    System.out.println("Password 'admin' verificado: " + encoder.matches("admin", hashAdmin));
		
	}

}
