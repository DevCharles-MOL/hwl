package com.historias_clinicas.hdw.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.historias_clinicas.hdw.service.UsuarioService;

@Controller
public class PaginaController {

	private final UsuarioService usuarioService;

	public PaginaController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/")
	public String inicioPublico() {
		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/registro")
	public String registro() {
		return "registro";
	}

	@PostMapping("/registro")
	public String registrar(@RequestParam String nombre, @RequestParam String email,
			@RequestParam String password, RedirectAttributes redirectAttributes) {
		try {
			usuarioService.registrar(nombre, email, password);
			redirectAttributes.addFlashAttribute("mensaje", "Cuenta creada. Ya puedes iniciar sesión.");
			return "redirect:/login";
		} catch (IllegalArgumentException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
			return "redirect:/registro";
		}
	}

	@GetMapping("/redirigir")
	public String redirigir(Authentication auth) {
		boolean admin = auth.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
		return admin ? "redirect:/admin" : "redirect:/usuario";
	}

	@GetMapping("/usuario")
	public String panelUsuario() {
		return "usuario";
	}

	@GetMapping("/admin")
	public String panelAdmin() {
		return "admin";
	}
}