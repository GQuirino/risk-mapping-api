package com.riskmapping.security;

import com.riskmapping.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http,
                                       JwtService jwtService,
                                       UsuarioRepository usuarioRepository) throws Exception {
        http
                .csrf(csrf -> csrf.disable())                 // API com token, sem cookies de sessão
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))) // sem login -> 401
                .authorizeHttpRequests(regras -> regras
                        // público: login, cadastro, Swagger e página de erro
                        .requestMatchers("/api/v1/auth/**", "/error",
                                "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        // público: o mapa pode ser visto sem login
                        .requestMatchers(HttpMethod.GET, "/api/v1/categorias/**",
                                "/api/v1/bairros/**", "/api/v1/riscos/**").permitAll()
                        // categorias e bairros: criar/editar/apagar só ADMIN
                        .requestMatchers("/api/v1/categorias/**", "/api/v1/bairros/**").hasRole("ADMIN")
                        // "meu perfil": qualquer usuário logado
                        .requestMatchers("/api/v1/usuarios/me/**").authenticated()
                        // gerenciar outros usuários: só ADMIN
                        .requestMatchers("/api/v1/usuarios/**").hasRole("ADMIN")
                        // o resto (criar/editar riscos, comentar e confirmar): logado
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtFilter(jwtService, usuarioRepository),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
