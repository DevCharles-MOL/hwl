package com.historias_clinicas.hdw.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.historias_clinicas.hdw.entity.RegistroPendiente;
import com.historias_clinicas.hdw.repository.RegistroPendienteRepository;

@ExtendWith(MockitoExtension.class)
class VerificacionRegistroServiceTest {
	private static final String EMAIL = "persona@example.com";
	private static final String CODIGO = "012345";
	@Mock RegistroPendienteRepository pendientes;
	@Mock UsuarioService usuarios;
	@Mock CorreoVerificacionService correo;
	private PasswordEncoder encoder;
	private VerificacionRegistroService service;

	@BeforeEach void setup() { encoder = new BCryptPasswordEncoder(4); service = new VerificacionRegistroService(pendientes, usuarios, encoder, correo, 5); }

	@Test void codigoCorrectoCreaCuentaYEliminaPendiente() {
		var p = nuevo(20, CODIGO); when(pendientes.buscarBloqueando(EMAIL)).thenReturn(Optional.of(p));
		service.verificar(EMAIL, CODIGO);
		verify(usuarios).crearVerificado(any(UsuarioService.RegistroPreparado.class)); verify(pendientes).delete(p);
	}
	@Test void codigoIncorrectoCuentaIntento() {
		var p = nuevo(20, CODIGO); when(pendientes.buscarBloqueando(EMAIL)).thenReturn(Optional.of(p));
		assertThrows(IllegalArgumentException.class, () -> service.verificar(EMAIL, "999999"));
		verify(pendientes).save(p); verify(usuarios, never()).crearVerificado(any());
	}
	@Test void codigoExpiradoSeInvalida() {
		var p = nuevo(301, CODIGO); when(pendientes.buscarBloqueando(EMAIL)).thenReturn(Optional.of(p));
		assertThrows(IllegalStateException.class, () -> service.verificar(EMAIL, CODIGO));
		assertNull(p.getCodigoHash()); verify(pendientes).save(p);
	}
	@Test void quintoIntentoInvalidaCodigo() {
		var p = nuevo(20, CODIGO); for (int i=0;i<4;i++) p.fallo(Instant.now());
		when(pendientes.buscarBloqueando(EMAIL)).thenReturn(Optional.of(p));
		assertThrows(IllegalStateException.class, () -> service.verificar(EMAIL, "999999"));
		assertNull(p.getCodigoHash());
	}
	@Test void reenvioGeneraCodigoNuevoYReemplazaHash() {
		var p = nuevo(61, CODIGO); when(pendientes.buscarBloqueando(EMAIL)).thenReturn(Optional.of(p));
		service.reenviar(EMAIL);
		ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
		verify(correo).enviar(eq(EMAIL), captor.capture());
		assertTrue(captor.getValue().matches("\\d{6}"));
		assertFalse(encoder.matches(CODIGO, p.getCodigoHash()));
		assertTrue(encoder.matches(captor.getValue(), p.getCodigoHash()));
	}
	private RegistroPendiente nuevo(long haceSegundos, String codigo) {
		return new RegistroPendiente("Persona", EMAIL, encoder.encode("clave-segura"), encoder.encode(codigo), Instant.now().minusSeconds(haceSegundos));
	}
}
