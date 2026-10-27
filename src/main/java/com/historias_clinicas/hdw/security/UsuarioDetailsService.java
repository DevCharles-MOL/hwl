package com.historias_clinicas.hdw.security;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.historias_clinicas.hdw.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
		return usuarioRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
				.map(u -> User.withUsername(u.getEmail())
						.password(u.getPassword())
						.roles(u.getRol().name()) // genera ROLE_USER / ROLE_ADMIN
						.build())
				.orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
	}
}
