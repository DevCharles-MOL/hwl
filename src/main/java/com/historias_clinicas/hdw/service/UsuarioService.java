package com.historias_clinicas.hdw.service;

import java.util.Locale;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.historias_clinicas.hdw.entity.Rol;
import com.historias_clinicas.hdw.entity.Usuario;
import com.historias_clinicas.hdw.repository.UsuarioRepository;

@Service
public class UsuarioService {

	private final UsuarioRepository usuarioRepository;
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public UsuarioService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public void registrar(String nombre, String email, String password) {
		String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
		if (usuarioRepository.existsByEmail(emailNormalizado)) {
			throw new IllegalArgumentException("Ya existe una cuenta con ese correo.");
		}

		Usuario usuario = new Usuario(nombre.trim(), emailNormalizado,
		passwordEncoder.encode(password), Rol.USER);
		usuarioRepository.save(usuario);
	}
}