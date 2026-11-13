package com.riskmapping.security;

import com.riskmapping.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// Gera e lê o token JWT
@Service
public class JwtService {

    private static final int TAMANHO_MINIMO_SEGREDO = 32;

    private final SecretKey chave;
    private final long expiracaoEmMs;

    public JwtService(@Value("${app.jwt.secret}") String segredo,
                      @Value("${app.jwt.expiracao-minutos}") long expiracaoEmMinutos) {
        if (segredo == null || segredo.length() < TAMANHO_MINIMO_SEGREDO) {
            throw new IllegalStateException(
                    "Defina app.jwt.secret em config/application.properties "
                            + "(ou a variável JWT_SECRET) com no mínimo "
                            + TAMANHO_MINIMO_SEGREDO + " caracteres");
        }
        this.chave = Keys.hmacShaKeyFor(segredo.getBytes(StandardCharsets.UTF_8));
        this.expiracaoEmMs = expiracaoEmMinutos * 60 * 1000;
    }

    public String gerar(Usuario usuario) {
        Date agora = new Date();
        return Jwts.builder()
                .subject(String.valueOf(usuario.id()))
                .issuedAt(agora)
                .expiration(new Date(agora.getTime() + expiracaoEmMs))
                .signWith(chave)
                .compact();
    }

    // Lança JwtException se o token for inválido ou estiver expirado
    public Claims ler(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(chave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims;
    }
}
