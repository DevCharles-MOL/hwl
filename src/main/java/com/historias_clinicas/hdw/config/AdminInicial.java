package com.historias_clinicas.hdw.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.historias_clinicas.hdw.entity.Rol;
import com.historias_clinicas.hdw.repository.UsuarioRepository;
import com.historias_clinicas.hdw.service.UsuarioService;

/** Crea un ADMIN al arrancar solo si defines ADMIN_EMAIL y ADMIN_PASSWORD en el entorno. */
@Component
public class AdminInicial implements CommandLineRunner {

	private final UsuarioService usuarioService;
	private final UsuarioRepository usuarioRepository;
	private final String email;
	private final String password;

	public AdminInicial(UsuarioService usuarioService, UsuarioRepository usuarioRepository,
			@Value("${app.admin.email:}") String email,
			@Value("${app.admin.password:}") String password) {
		this.usuarioService = usuarioService;
		this.usuarioRepository = usuarioRepository;
		this.email = email;
		this.password = password;
	}

	@Override
	public void run(String... args) {
		if (email.isBlank() || password.isBlank()) {
			return;
		}
		if (!usuarioRepository.existsByEmail(email.trim().toLowerCase())) {
			usuarioService.crear("Administrador", email, password, Rol.ADMIN);
		}
	}
}
