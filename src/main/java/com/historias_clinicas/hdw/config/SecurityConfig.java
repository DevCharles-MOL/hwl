package com.historias_clinicas.hdw.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import com.historias_clinicas.hdw.repository.UsuarioRepository;
import com.historias_clinicas.hdw.security.MfaFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, UsuarioRepository usuarioRepository)
			throws Exception {
		http
			.authorizeHttpRequests(auth -> auth
				.requestMatchers("/", "/login", "/registro", "/css/**", "/error").permitAll()
				.requestMatchers("/admin/**").hasRole("ADMIN")
				.anyRequest().authenticated())
			.formLogin(form -> form
				.loginPage("/login")
				.successHandler((request, response, authentication) -> {
					// Cada login nuevo debe pasar el segundo factor otra vez.
					request.getSession().removeAttribute(MfaFilter.MFA_VERIFICADO);
					response.sendRedirect(request.getContextPath() + "/redirigir");
				})
				.permitAll())
			.logout(logout -> logout.logoutSuccessUrl("/login?logout").permitAll())
			.addFilterBefore(new MfaFilter(usuarioRepository), AuthorizationFilter.class);
		return http.build();
	}
}
