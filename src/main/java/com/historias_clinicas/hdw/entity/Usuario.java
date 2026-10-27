package com.historias_clinicas.hdw.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nombre;

	@Column(nullable = false, unique = true)
	private String email;

	@Column(nullable = false)
	private String password;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Rol rol = Rol.USER;

	// Secreto TOTP guardado CIFRADO (nunca en texto plano).
	@Column(name = "totp_secret", length = 512)
	private String totpSecret;

	@Column(name = "mfa_activo", nullable = false, columnDefinition = "boolean default false")
	private boolean mfaActivo;

	protected Usuario() {
	}

	public Usuario(String nombre, String email, String password, Rol rol) {
		this.nombre = nombre;
		this.email = email;
		this.password = password;
		this.rol = rol;
	}

	public void activarMfa(String secretCifrado) {
		this.totpSecret = secretCifrado;
		this.mfaActivo = true;
	}

	public Long getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public Rol getRol() {
		return rol;
	}

	public String getTotpSecret() {
		return totpSecret;
	}

	public boolean isMfaActivo() {
		return mfaActivo;
	}
}
