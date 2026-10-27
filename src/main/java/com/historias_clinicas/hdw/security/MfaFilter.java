package com.historias_clinicas.hdw.security;

import java.io.IOException;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.historias_clinicas.hdw.entity.Usuario;
import com.historias_clinicas.hdw.repository.UsuarioRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Si el usuario ya pasó usuario/contraseña pero todavía no el segundo factor,
 * lo manda a /mfa/verificar (o /mfa/configurar si aún no activó MFA).
 * No es un @Component a propósito: se registra solo dentro de la cadena de Spring Security.
 */
public class MfaFilter extends OncePerRequestFilter {

	public static final String MFA_VERIFICADO = "MFA_VERIFICADO";

	private final UsuarioRepository usuarioRepository;

	public MfaFilter(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		boolean autenticado = auth != null && auth.isAuthenticated()
				&& !(auth instanceof AnonymousAuthenticationToken);

		if (autenticado && !rutaPermitida(request)) {
			HttpSession session = request.getSession(false);
			boolean verificado = session != null
					&& Boolean.TRUE.equals(session.getAttribute(MFA_VERIFICADO));
			if (!verificado) {
				boolean activo = usuarioRepository.findByEmail(auth.getName())
						.map(Usuario::isMfaActivo).orElse(false);
				response.sendRedirect(request.getContextPath()
						+ (activo ? "/mfa/verificar" : "/mfa/configurar"));
				return;
			}
		}
		chain.doFilter(request, response);
	}

	private boolean rutaPermitida(HttpServletRequest request) {
		String path = request.getRequestURI().substring(request.getContextPath().length());
		return path.startsWith("/mfa/") || path.equals("/logout")
				|| path.startsWith("/css/") || path.equals("/error");
	}
}
