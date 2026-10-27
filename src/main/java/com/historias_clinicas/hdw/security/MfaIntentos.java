package com.historias_clinicas.hdw.security;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/** Limita intentos fallidos del código TOTP (en memoria; en producción usa Redis o BD). */
@Component
public class MfaIntentos {

	private static final int MAX_FALLOS = 5;
	private static final Duration BLOQUEO = Duration.ofMinutes(5);

	private record Registro(int fallos, Instant bloqueadoHasta) {
	}

	private final ConcurrentHashMap<String, Registro> registros = new ConcurrentHashMap<>();

	public boolean bloqueado(String email) {
		Registro r = registros.get(email);
		if (r == null || r.bloqueadoHasta() == null) {
			return false;
		}
		if (Instant.now().isAfter(r.bloqueadoHasta())) {
			registros.remove(email);
			return false;
		}
		return true;
	}

	public void fallo(String email) {
		registros.merge(email, new Registro(1, null), (viejo, nuevo) -> {
			int fallos = viejo.fallos() + 1;
			return fallos >= MAX_FALLOS
					? new Registro(fallos, Instant.now().plus(BLOQUEO))
					: new Registro(fallos, null);
		});
	}

	public void exito(String email) {
		registros.remove(email);
	}
}
