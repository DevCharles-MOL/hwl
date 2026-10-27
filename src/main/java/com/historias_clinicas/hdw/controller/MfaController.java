package com.historias_clinicas.hdw.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.historias_clinicas.hdw.security.MfaFilter;
import com.historias_clinicas.hdw.security.MfaIntentos;
import com.historias_clinicas.hdw.service.MfaService;

import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mfa")
public class MfaController {

	private static final String SECRETO_PENDIENTE = "MFA_SECRETO_PENDIENTE";
	private static final String MSG_BLOQUEO = "Demasiados intentos fallidos. Espera unos minutos.";
	private static final String MSG_CODIGO = "Código incorrecto. Revisa que la hora de tu celular esté en automático.";

	private final MfaService mfaService;
	private final MfaIntentos intentos;

	public MfaController(MfaService mfaService, MfaIntentos intentos) {
		this.mfaService = mfaService;
		this.intentos = intentos;
	}

	// ---------- Activación (primer login) ----------

	@GetMapping("/configurar")
	public String configurar(Authentication auth, HttpSession session, Model model)
			throws QrGenerationException {
		if (mfaService.tieneMfa(auth.getName())) {
			return "redirect:/mfa/verificar";
		}
		String secreto = (String) session.getAttribute(SECRETO_PENDIENTE);
		if (secreto == null) {
			secreto = mfaService.generarSecreto();
			session.setAttribute(SECRETO_PENDIENTE, secreto);
		}
		model.addAttribute("qr", mfaService.qrComoDataUri(auth.getName(), secreto));
		model.addAttribute("secreto", secreto);
		return "mfa-configurar";
	}

	@PostMapping("/configurar")
	public String confirmar(@RequestParam String codigo, Authentication auth, HttpSession session,
			RedirectAttributes redirectAttributes) {
		String email = auth.getName();
		String secreto = (String) session.getAttribute(SECRETO_PENDIENTE);
		if (secreto == null) {
			return "redirect:/mfa/configurar";
		}
		if (intentos.bloqueado(email)) {
			redirectAttributes.addFlashAttribute("error", MSG_BLOQUEO);
			return "redirect:/mfa/configurar";
		}
		if (!mfaService.codigoValido(secreto, codigo)) {
			intentos.fallo(email);
			redirectAttributes.addFlashAttribute("error", MSG_CODIGO);
			return "redirect:/mfa/configurar";
		}
		mfaService.activar(email, secreto);
		intentos.exito(email);
		session.removeAttribute(SECRETO_PENDIENTE);
		session.setAttribute(MfaFilter.MFA_VERIFICADO, Boolean.TRUE);
		return "redirect:/redirigir";
	}

	// ---------- Verificación (cada login) ----------

	@GetMapping("/verificar")
	public String verificar(Authentication auth, HttpSession session) {
		if (Boolean.TRUE.equals(session.getAttribute(MfaFilter.MFA_VERIFICADO))) {
			return "redirect:/redirigir";
		}
		if (!mfaService.tieneMfa(auth.getName())) {
			return "redirect:/mfa/configurar";
		}
		return "mfa-verificar";
	}

	@PostMapping("/verificar")
	public String validar(@RequestParam String codigo, Authentication auth, HttpSession session,
			RedirectAttributes redirectAttributes) {
		String email = auth.getName();
		if (intentos.bloqueado(email)) {
			redirectAttributes.addFlashAttribute("error", MSG_BLOQUEO);
			return "redirect:/mfa/verificar";
		}
		if (!mfaService.verificarUsuario(email, codigo)) {
			intentos.fallo(email);
			redirectAttributes.addFlashAttribute("error", MSG_CODIGO);
			return "redirect:/mfa/verificar";
		}
		intentos.exito(email);
		session.setAttribute(MfaFilter.MFA_VERIFICADO, Boolean.TRUE);
		return "redirect:/redirigir";
	}
}
