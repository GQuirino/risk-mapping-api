package com.riskmapping.security;

import com.riskmapping.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

// Roda a cada requisição: se vier "Authorization: Bearer <token>" válido,
// marca o usuário como logado. Se não vier (ou for inválido), segue sem login
// e o SecurityConfig decide se a rota exigia login (-> 401).
public class JwtFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;

    public JwtFilter(JwtService jwtService, UsuarioRepository usuarioRepository) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");

        if (cabecalho != null && cabecalho.startsWith("Bearer ")) {
            try {
                Claims claims = jwtService.ler(cabecalho.substring(7));
                Long usuarioId = Long.valueOf(claims.getSubject());

                // Busca no repositório: se o usuário foi apagado, o token deixa de valer
                usuarioRepository.buscarPorId(usuarioId).ifPresent(usuario -> {
                    var principal = new UsuarioAutenticado(usuario.id(), usuario.perfil());
                    var autoridade = new SimpleGrantedAuthority("ROLE_" + usuario.perfil());
                    var autenticacao = new UsernamePasswordAuthenticationToken(
                            principal, null, List.of(autoridade));
                    SecurityContextHolder.getContext().setAuthentication(autenticacao);
                });
            } catch (JwtException | IllegalArgumentException e) {
                // token inválido ou expirado: ignora, a requisição segue sem login
            }
        }

        chain.doFilter(request, response);
    }
}
