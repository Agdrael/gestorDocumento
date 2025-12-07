package com.prograIV.gestorDocumento.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable()) // necesario para logout por GET
            .userDetailsService(userDetailsService)
            .authorizeHttpRequests(auth -> auth

                // Recursos estáticos
                .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()

                // LOGIN + PROCESAMIENTO DE LOGIN RESTAURADO
                .requestMatchers("/login", "/procesar-login").permitAll()

                // Registro (si lo usas)
                .requestMatchers("/usuarios/crear").permitAll()

                // Rutas protegidas
                .requestMatchers("/perfil", "/perfil/**").authenticated()
                .requestMatchers("/api/admin/**", "/dashboard-admin").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/documentos/**").authenticated()

                // Todo lo demás requiere sesión
                .anyRequest().authenticated()
            )

            // --- LOGIN RESTAURADO COMO LA VERSIÓN QUE FUNCIONABA ---
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/procesar-login") // <- ESTA ES LA LÍNEA CLAVE
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // --- LOGOUT ---
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .permitAll()
            );

        return http.build();
    }
}
