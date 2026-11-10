package com.historias_clinicas.hdw.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.historias_clinicas.hdw.entity.RegistroPendiente;
import jakarta.persistence.LockModeType;

public interface RegistroPendienteRepository extends JpaRepository<RegistroPendiente, Long> {
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from RegistroPendiente p where p.email = :email")
	Optional<RegistroPendiente> buscarBloqueando(@Param("email") String email);
	long deleteByActualizadoEnBefore(Instant limite);
}
