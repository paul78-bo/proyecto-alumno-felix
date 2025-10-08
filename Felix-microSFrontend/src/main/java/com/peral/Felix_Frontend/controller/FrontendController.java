package com.peral.Felix_Frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.*;

// ✅ AGREGAR ESTE IMPORT
import com.peral.Felix_Frontend.model.Alumno;

@Controller
public class FrontendController {

	private final RestTemplate restTemplate;
	private final String GATEWAY_URL = "http://localhost:8080";

	public FrontendController(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	// ✅ Página de login
	@GetMapping("/login")
	public String loginPage(Model model, @RequestParam(value = "error", required = false) String error) {
		if (error != null) {
			model.addAttribute("error", "Credenciales incorrectas");
		}
		return "login";
	}

	// ✅ Procesar login y obtener JWT - CORREGIDO
	@PostMapping("/login")
	public String processLogin(@RequestParam String username, @RequestParam String password, HttpSession session,
			Model model) {
		try {
			// Crear request de login
			Map<String, String> loginRequest = new HashMap<>();
			loginRequest.put("username", username);
			loginRequest.put("password", password);

			String loginUrl = GATEWAY_URL + "/auth/login";

			// Hacer petición de login
			ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, loginRequest, Map.class);

			if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
				Map<String, Object> responseBody = response.getBody();

				// ✅ EXTRAER TOKEN Y ROLE CORRECTAMENTE
				String token = (String) responseBody.get("token");
				String role = (String) responseBody.get("role");

				// Guardar en sesión
				session.setAttribute("username", username);
				session.setAttribute("token", token);
				session.setAttribute("role", role != null ? role : "USER");
				session.setAttribute("authenticated", true);

				return "redirect:/alumnos";
			} else {
				model.addAttribute("error", "Credenciales incorrectas");
				return "login";
			}

		} catch (HttpClientErrorException.Unauthorized e) {
			model.addAttribute("error", "Usuario o contraseña incorrectos");
			return "login";
		} catch (HttpClientErrorException e) {
			model.addAttribute("error", "Error de autenticación: " + e.getStatusCode());
			return "login";
		} catch (Exception e) {
			model.addAttribute("error", "Error de conexión: " + e.getMessage());
			return "login";
		}
	}

	// ✅ Página principal
	@GetMapping("/")
	public String home(HttpSession session) {
		if (!isAuthenticated(session)) {
			return "redirect:/login";
		}
		return "redirect:/alumnos";
	}

	// ✅ Lista de alumnos CON JWT - CORREGIDO
	@GetMapping("/alumnos")
	public String listarAlumnos(Model model, HttpSession session) {

	    System.out.println("🔵 GET /alumnos - Usuario: " + session.getAttribute("username"));
	    System.out.println("🔵 Rol: " + session.getAttribute("role"));
	    System.out.println("🔵 Autenticado: " + session.getAttribute("authenticated"));

	    // Verificar autenticación
	    if (!isAuthenticated(session)) {
	        return "redirect:/login";
	    }

	    try {
	        String token = (String) session.getAttribute("token");
	        String role = (String) session.getAttribute("role");

	        // Crear headers con JWT
	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + token);
	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        // Hacer petición con token
	        ResponseEntity<Alumno[]> response = restTemplate.exchange(GATEWAY_URL + "/alumnos", HttpMethod.GET, entity,
	                Alumno[].class);

	        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
	            List<Alumno> alumnos = Arrays.asList(response.getBody());
	            model.addAttribute("alumnos", alumnos);
	        } else {
	            model.addAttribute("alumnos", new ArrayList<>());
	            model.addAttribute("error", "No se pudieron cargar los alumnos");
	        }

	        // ✅ CORREGIR: Lógica más robusta para roles
	        String userRole = (String) session.getAttribute("role");
	        System.out.println("🔵 Rol del usuario: " + userRole);
	        
	        boolean isAdmin = userRole != null && userRole.contains("ADMIN");
	        boolean isProfesor = userRole != null && (userRole.contains("PROFESOR") || userRole.contains("PROFESO"));
	        boolean isAlumno = userRole != null && userRole.contains("ALUMNO");

	        System.out.println("🔵 isAdmin: " + isAdmin);
	        System.out.println("🔵 isProfesor: " + isProfesor);
	        System.out.println("🔵 isAlumno: " + isAlumno);

	        model.addAttribute("isAdmin", isAdmin);
	        model.addAttribute("isProfesor", isProfesor);
	        model.addAttribute("isAlumno", isAlumno);

	    } catch (HttpClientErrorException.Unauthorized e) {
	        session.invalidate();
	        return "redirect:/login?error=unauthorized";
	    } catch (Exception e) {
	        model.addAttribute("alumnos", new ArrayList<>());
	        model.addAttribute("error", "Error al cargar alumnos: " + e.getMessage());
	    }

	    // ✅ AGREGAR DATOS DE USUARIO AL MODELO
	    model.addAttribute("username", session.getAttribute("username"));
	    model.addAttribute("role", session.getAttribute("role"));

	    return "alumnos";
	}
	// ✅ Guardar alumno CON JWT - CORREGIDO
	// ✅ Guardar alumno CON JWT - USAR @ModelAttribute para formularios HTML
	// ✅ Guardar alumno CON JWT - USAR @ModelAttribute para formularios HTML
	@PostMapping("/alumnos")
	public String guardarAlumno(@ModelAttribute Alumno alumno, // ← CAMBIO AQUÍ
			HttpSession session, Model model) {
		System.out.println("🔵 POST /alumnos recibido");
		System.out.println("📝 Datos alumno: " + alumno);
		System.out.println("📝 Nombre: " + alumno.getNombre());
		System.out.println("📝 Email: " + alumno.getEmail());
		System.out.println("📝 Edad: " + alumno.getEdad());
		System.out.println("📝 Carrera: " + alumno.getCarrera());

		if (!isAuthenticated(session)) {
			return "redirect:/login";
		}

		try {
			String token = (String) session.getAttribute("token");

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + token);
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<Alumno> entity = new HttpEntity<>(alumno, headers);

			ResponseEntity<Alumno> response = restTemplate.postForEntity(GATEWAY_URL + "/alumnos", entity,
					Alumno.class);

			if (response.getStatusCode() != HttpStatus.OK) {
				throw new RuntimeException("Error al crear alumno: " + response.getStatusCode());
			}

			System.out.println("✅ Alumno guardado exitosamente");
			return "redirect:/alumnos";

		} catch (Exception e) {
			System.err.println("❌ Error guardando alumno: " + e.getMessage());
			e.printStackTrace();
			model.addAttribute("error", "Error al guardar alumno: " + e.getMessage());
			model.addAttribute("alumno", alumno);
			return "form-alumno";
		}
	}

	// ✅ Formulario para nuevo alumno - Solo Admin y Profesor
	@GetMapping("/alumnos/nuevo")
	public String mostrarFormularioNuevo(Model model, HttpSession session) {
	    if (!isAuthenticated(session)) {
	        return "redirect:/login";
	    }
	    
	    String role = (String) session.getAttribute("role");
	    if ("ROLE_ALUMNO".equals(role)) {
	        return "redirect:/alumnos?error=No tienes permisos para crear alumnos";
	    }
	    
	    model.addAttribute("alumno", new Alumno());
	    return "form-alumno";
	}

	// ✅ Editar alumno - Solo Admin y Profesor
	@GetMapping("/alumnos/editar/{id}")
	public String editarAlumno(@PathVariable Long id, Model model, HttpSession session) {
	    if (!isAuthenticated(session)) {
	        return "redirect:/login";
	    }

	    String role = (String) session.getAttribute("role");
	    if ("ROLE_ALUMNO".equals(role)) {
	        return "redirect:/alumnos?error=No tienes permisos para editar alumnos";
	    }

	    try {
	        String token = (String) session.getAttribute("token");

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + token);
	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        ResponseEntity<Alumno> response = restTemplate.exchange(GATEWAY_URL + "/alumnos/" + id, HttpMethod.GET,
	                entity, Alumno.class);

	        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
	            model.addAttribute("alumno", response.getBody());
	            return "form-alumno";
	        } else {
	            return "redirect:/alumnos";
	        }

	    } catch (Exception e) {
	        return "redirect:/alumnos";
	    }
	}

	// ✅ Eliminar alumno - Solo Admin y Profesor
	@GetMapping("/alumnos/eliminar/{id}")
	public String eliminarAlumno(@PathVariable Long id, HttpSession session) {
	    if (!isAuthenticated(session)) {
	        return "redirect:/login";
	    }

	    String role = (String) session.getAttribute("role");
	    if ("ROLE_ALUMNO".equals(role)) {
	        return "redirect:/alumnos?error=No tienes permisos para eliminar alumnos";
	    }

	    try {
	        String token = (String) session.getAttribute("token");

	        HttpHeaders headers = new HttpHeaders();
	        headers.set("Authorization", "Bearer " + token);
	        HttpEntity<String> entity = new HttpEntity<>(headers);

	        restTemplate.exchange(GATEWAY_URL + "/alumnos/" + id, HttpMethod.DELETE, entity, Void.class);

	    } catch (Exception e) {
	        // Log error pero continuar
	        System.err.println("Error al eliminar alumno: " + e.getMessage());
	    }

	    return "redirect:/alumnos";
	}
	
	// ✅ Cerrar sesión
	@GetMapping("/logout")
	public String logout(HttpSession session) {
		if (session != null) {
			session.invalidate();
		}
		return "redirect:/login";
	}

	// ✅ Helper para verificar autenticación
	private boolean isAuthenticated(HttpSession session) {
		return session != null && session.getAttribute("authenticated") != null
				&& Boolean.TRUE.equals(session.getAttribute("authenticated"));
	}
}
// ✅ QUITAR LA LLAVE EXTRA QUE TENÍAS