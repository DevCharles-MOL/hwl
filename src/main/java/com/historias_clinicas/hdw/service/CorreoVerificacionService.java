package com.historias_clinicas.hdw.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class CorreoVerificacionService {
	private final JavaMailSender sender;
	private final String remitente;
	public CorreoVerificacionService(JavaMailSender sender, @Value("${app.mail.from}") String remitente) { this.sender = sender; this.remitente = remitente; }
	public void enviar(String destino, String codigo) {
		SimpleMailMessage m = new SimpleMailMessage(); m.setFrom(remitente); m.setTo(destino);
		m.setSubject("Código de verificación - Historias Clínicas");
		m.setText("Hola,\n\nTu código de verificación es: " + codigo + "\n\nVence en 5 minutos y solo puede usarse una vez. Si no solicitaste este registro, ignora este mensaje.");
		sender.send(m);
	}
}
