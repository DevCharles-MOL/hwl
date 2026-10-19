package com.historias_clinicas.hdw.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.historias_clinicas.hdw.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
	Optional<Usuario> findByEmail(String email);
	boolean existsByEmail(String email);
}