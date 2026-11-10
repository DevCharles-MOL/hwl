package com.historias_clinicas.hdw.config;

import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.historias_clinicas.hdw.service.VerificacionRegistroService;

@Component
public class LimpiezaRegistroPendiente {
	private final VerificacionRegistroService service;
	public LimpiezaRegistroPendiente(VerificacionRegistroService service) { this.service = service; }
	@Scheduled(fixedDelayString = "${app.registration.cleanup-delay-ms:900000}")
	public void limpiar() { service.limpiarAntiguos(Instant.now()); }
}
