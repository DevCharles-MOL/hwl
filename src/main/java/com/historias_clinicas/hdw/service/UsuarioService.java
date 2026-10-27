package com.historias_clinicas.hdw.service;

import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.historias_clinicas.hdw.entity.Rol;
import com.historias_clinicas.hdw.entity.Usuario;
import com.historias_clinicas.hdw.repository.UsuarioRepository;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public void registrar(String nombre, String email, String password) {
		crear(nombre, email, password, Rol.USER);
	}

	@Transactional
	public void crear(String nombre, String email, String password, Rol rol) {
		if (nombre == null || nombre.isBlank()) {
			throw new IllegalArgumentException("El nombre es obligatorio.");
		}
		if (password == null || password.length() < 8) {
			throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres.");
		}
		String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existsByEmail(emailNormalizado)) {
			throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
		}
		usuarioRepository.save(new Usuario(nombre.trim(), emailNormalizado,
				passwordEncoder.encode(password), rol));
	}
}
