package com.historias_clinicas.hdw.controller;

import org.springframework.security.core.Authentication;
import org.springframework.mail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import com.historias_clinicas.hdw.service.VerificacionRegistroService;

@Controller
public class PaginaController {
	private static final Logger log = LoggerFactory.getLogger(PaginaController.class);
	private static final String EMAIL_PENDIENTE = "EMAIL_REGISTRO_PENDIENTE";
	private final VerificacionRegistroService verificacion;

	public PaginaController(VerificacionRegistroService verificacion) {
		this.verificacion = verificacion;
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
			@RequestParam String password, HttpSession session, RedirectAttributes redirectAttributes) {
		try {
			verificacion.iniciar(nombre, email, password);
			session.setAttribute(EMAIL_PENDIENTE, email.trim().toLowerCase(java.util.Locale.ROOT));
			redirectAttributes.addFlashAttribute("mensaje", "Enviamos un código de verificación a tu correo.");
			return "redirect:/registro/verificar";
		} catch (IllegalArgumentException | IllegalStateException exception) {
			redirectAttributes.addFlashAttribute("error", exception.getMessage());
			return "redirect:/registro";
		} catch (MailException exception) {
			log.warn("Falló el envío del código. Causa: {}", rootCause(exception));
			redirectAttributes.addFlashAttribute("error", "No pudimos enviar el correo. Revisa la configuración e inténtalo de nuevo.");
			return "redirect:/registro";
		}
	}

	@GetMapping("/registro/verificar")
	public String verificacion(HttpSession session, org.springframework.ui.Model model, RedirectAttributes flash) {
		String email = (String) session.getAttribute(EMAIL_PENDIENTE);
		if (email == null) { flash.addFlashAttribute("error", "Inicia el registro para solicitar un código."); return "redirect:/registro"; }
		int at = email.indexOf('@');
		model.addAttribute("emailEnmascarado", at > 1 ? email.substring(0, 1) + "•••" + email.substring(at) : "•••");
		return "registro-verificar";
	}

	@PostMapping("/registro/verificar")
	public String confirmar(@RequestParam String codigo, HttpSession session, RedirectAttributes flash) {
		String email = (String) session.getAttribute(EMAIL_PENDIENTE);
		if (email == null) return "redirect:/registro";
		try {
			verificacion.verificar(email, codigo);
			session.removeAttribute(EMAIL_PENDIENTE);
			flash.addFlashAttribute("mensaje", "Correo verificado y cuenta creada. Ya puedes iniciar sesión.");
			return "redirect:/login";
		} catch (IllegalArgumentException | IllegalStateException e) {
			flash.addFlashAttribute("error", e.getMessage()); return "redirect:/registro/verificar";
		}
	}

	@PostMapping("/registro/reenviar")
	public String reenviar(HttpSession session, RedirectAttributes flash) {
		String email = (String) session.getAttribute(EMAIL_PENDIENTE);
		if (email == null) return "redirect:/registro";
		try { verificacion.reenviar(email); flash.addFlashAttribute("mensaje", "Enviamos un código nuevo."); }
		catch (IllegalStateException e) { flash.addFlashAttribute("error", e.getMessage()); }
		catch (MailException e) { log.warn("Falló el reenvío del código. Causa: {}", rootCause(e)); flash.addFlashAttribute("error", "No pudimos enviar el correo. Inténtalo más tarde."); }
		return "redirect:/registro/verificar";
	}

	private String rootCause(Throwable t) { while (t.getCause() != null && t.getCause() != t) t = t.getCause(); return t.getClass().getSimpleName(); }

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
