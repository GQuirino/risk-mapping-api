package com.riskmapping.security;

import com.riskmapping.model.Perfil;

// Quem está logado na requisição atual (montado pelo JwtFilter)
public record UsuarioAutenticado(Long id, Perfil perfil) {
}
