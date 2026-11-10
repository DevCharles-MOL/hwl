package com.historias_clinicas.hdw.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "registros_pendientes", indexes = @Index(name = "idx_registro_pendiente_actualizado", columnList = "actualizado_en"))
public class RegistroPendiente {
	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(nullable = false, length = 120) private String nombre;
	@Column(nullable = false, unique = true, length = 180) private String email;
	@Column(name = "password_hash", nullable = false, length = 100) private String passwordHash;
	@Column(name = "codigo_hash", length = 100) private String codigoHash;
	@Column(name = "expira_en", nullable = false) private Instant expiraEn;
	@Column(nullable = false) private int intentos;
	@Column(name = "ultimo_envio", nullable = false) private Instant ultimoEnvio;
	@Column(name = "inicio_ventana_reenvios", nullable = false) private Instant inicioVentana;
	@Column(name = "cantidad_reenvios", nullable = false) private int cantidadReenvios;
	@Column(name = "actualizado_en", nullable = false) private Instant actualizadoEn;

	protected RegistroPendiente() { }

	public RegistroPendiente(String nombre, String email, String passwordHash, String codigoHash, Instant ahora) {
		this.nombre = nombre; this.email = email; this.passwordHash = passwordHash;
		this.codigoHash = codigoHash; this.expiraEn = ahora.plusSeconds(300);
		this.ultimoEnvio = ahora; this.inicioVentana = ahora; this.actualizadoEn = ahora;
	}

	public void reenviar(String hash, Instant ahora, int maxPorHora) {
		if (inicioVentana.plusSeconds(3600).isBefore(ahora)) { inicioVentana = ahora; cantidadReenvios = 0; }
		if (cantidadReenvios >= maxPorHora) throw new IllegalStateException("Alcanzaste el límite de reenvíos por hora. Inténtalo más tarde.");
		if (ultimoEnvio.plusSeconds(60).isAfter(ahora)) throw new IllegalStateException("Espera un minuto antes de solicitar otro código.");
		codigoHash = hash; expiraEn = ahora.plusSeconds(300); intentos = 0;
		cantidadReenvios++; ultimoEnvio = ahora; actualizadoEn = ahora;
	}
	public boolean expirado(Instant ahora) { return !ahora.isBefore(expiraEn); }
	public void invalidar(Instant ahora) { codigoHash = null; expiraEn = ahora; actualizadoEn = ahora; }
	public boolean fallo(Instant ahora) { intentos++; actualizadoEn = ahora; if (intentos >= 5) { invalidar(ahora); return true; } return false; }
	public String getNombre() { return nombre; }
	public String getEmail() { return email; }
	public String getPasswordHash() { return passwordHash; }
	public String getCodigoHash() { return codigoHash; }
	public Instant getActualizadoEn() { return actualizadoEn; }
}
