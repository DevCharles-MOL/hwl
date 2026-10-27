package com.historias_clinicas.hdw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.historias_clinicas.hdw.entity.Usuario;
import com.historias_clinicas.hdw.repository.UsuarioRepository;

import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.util.Utils;

@Service
public class MfaService {

	private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
	private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
	private final CodeVerifier verifier = new DefaultCodeVerifier(
			new DefaultCodeGenerator(), new SystemTimeProvider());

	private final TextEncryptor encryptor;
	private final UsuarioRepository usuarioRepository;
	private final String issuer;

	public MfaService(UsuarioRepository usuarioRepository,
			@Value("${app.mfa.issuer}") String issuer,
			@Value("${app.mfa.encryption-password}") String password,
			@Value("${app.mfa.encryption-salt}") String saltHex) {
		this.usuarioRepository = usuarioRepository;
		this.issuer = issuer;
		this.encryptor = Encryptors.delux(password, saltHex);
	}

	public String generarSecreto() {
		return secretGenerator.generate();
	}

	public String qrComoDataUri(String email, String secreto) throws QrGenerationException {
		QrData data = new QrData.Builder()
				.label(email)
				.secret(secreto)
				.issuer(issuer)
				.algorithm(HashingAlgorithm.SHA1) // Microsoft Authenticator usa SHA1 / 6 dígitos / 30 s
				.digits(6)
				.period(30)
				.build();
		return Utils.getDataUriForImage(qrGenerator.generate(data), qrGenerator.getImageMimeType());
	}

	public boolean codigoValido(String secreto, String codigo) {
		if (codigo == null) {
			return false;
		}
		String limpio = codigo.replaceAll("\\s", "");
		return limpio.matches("\\d{6}") && verifier.isValidCode(secreto, limpio);
	}

	@Transactional(readOnly = true)
	public boolean tieneMfa(String email) {
		return usuarioRepository.findByEmail(email).map(Usuario::isMfaActivo).orElse(false);
	}

	@Transactional
	public void activar(String email, String secretoPlano) {
		Usuario usuario = usuarioRepository.findByEmail(email).orElseThrow();
		usuario.activarMfa(encryptor.encrypt(secretoPlano));
		usuarioRepository.save(usuario);
	}

	@Transactional(readOnly = true)
	public boolean verificarUsuario(String email, String codigo) {
		return usuarioRepository.findByEmail(email)
				.filter(Usuario::isMfaActivo)
				.map(u -> codigoValido(encryptor.decrypt(u.getTotpSecret()), codigo))
				.orElse(false);
	}
}
