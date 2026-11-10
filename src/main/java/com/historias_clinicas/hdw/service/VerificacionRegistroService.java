package com.historias_clinicas.hdw.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.historias_clinicas.hdw.entity.RegistroPendiente;
import com.historias_clinicas.hdw.repository.RegistroPendienteRepository;

@Service
public class VerificacionRegistroService {
	private static final SecureRandom RANDOM = new SecureRandom();
	private final RegistroPendienteRepository pendientes;
	private final UsuarioService usuarios;
	private final PasswordEncoder encoder;
	private final CorreoVerificacionService correo;
	private final Clock clock = Clock.systemUTC();
	private final int maxReenvios;

	public VerificacionRegistroService(RegistroPendienteRepository pendientes, UsuarioService usuarios,
			PasswordEncoder encoder, CorreoVerificacionService correo,
			@Value("${app.registration.max-resends-per-hour:5}") int maxReenvios) {
		this.pendientes = pendientes; this.usuarios = usuarios; this.encoder = encoder;
		this.correo = correo; this.maxReenvios = Math.max(1, maxReenvios);
	}

	@Transactional
	public void iniciar(String nombre, String email, String password) {
		var r = usuarios.prepararRegistro(nombre, email, password); Instant ahora = clock.instant();
		limpiarAntiguos(ahora);
		var previo = pendientes.buscarBloqueando(r.email()).orElse(null);
		if (previo != null) { enviarNuevo(previo, ahora); return; }
		String codigo = codigo();
		pendientes.save(new RegistroPendiente(r.nombre(), r.email(), r.passwordHash(), encoder.encode(codigo), ahora));
		correo.enviar(r.email(), codigo);
	}

	@Transactional
	public void reenviar(String email) {
		Instant ahora = clock.instant(); limpiarAntiguos(ahora);
		var p = pendientes.buscarBloqueando(normalizar(email)).orElseThrow(() -> new IllegalStateException("El registro pendiente venció. Inicia el registro de nuevo."));
		enviarNuevo(p, ahora);
	}

	@Transactional(noRollbackFor = {IllegalArgumentException.class, IllegalStateException.class})
	public void verificar(String email, String codigo) {
		Instant ahora = clock.instant(); limpiarAntiguos(ahora);
		var p = pendientes.buscarBloqueando(normalizar(email)).orElseThrow(() -> new IllegalStateException("El registro venció. Inicia el registro de nuevo."));
		if (p.expirado(ahora) || p.getCodigoHash() == null) { p.invalidar(ahora); pendientes.save(p); throw new IllegalStateException("El código venció o fue invalidado. Solicita uno nuevo."); }
		String limpio = codigo == null ? "" : codigo.trim();
		if (!limpio.matches("\\d{6}") || !encoder.matches(limpio, p.getCodigoHash())) {
			boolean agotado = p.fallo(ahora); pendientes.save(p);
			if (agotado) throw new IllegalStateException("Superaste los 5 intentos. Solicita un código nuevo.");
			throw new IllegalArgumentException("El código es incorrecto. Inténtalo de nuevo.");
		}
		usuarios.crearVerificado(new UsuarioService.RegistroPreparado(p.getNombre(), p.getEmail(), p.getPasswordHash()));
		pendientes.delete(p);
	}

	@Transactional
	public long limpiarAntiguos(Instant ahora) { return pendientes.deleteByActualizadoEnBefore(ahora.minus(Duration.ofHours(1))); }
	private void enviarNuevo(RegistroPendiente p, Instant ahora) { String c = codigo(); p.reenviar(encoder.encode(c), ahora, maxReenvios); pendientes.save(p); correo.enviar(p.getEmail(), c); }
	private String codigo() { return String.format(Locale.ROOT, "%06d", RANDOM.nextInt(1_000_000)); }
	private String normalizar(String e) { return e == null ? "" : e.trim().toLowerCase(Locale.ROOT); }
}
