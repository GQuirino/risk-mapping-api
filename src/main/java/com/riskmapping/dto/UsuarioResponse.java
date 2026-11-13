package com.riskmapping.dto;

import com.riskmapping.model.Perfil;
import com.riskmapping.model.Usuario;

// O que a API devolve de um usuário: sem a senha.
public record UsuarioResponse(Long id, String nome, String email, Perfil perfil) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(usuario.id(), usuario.nome(), usuario.email(), usuario.perfil());
    }
}
