package com.riskmapping.model;

// senhaHash é o hash BCrypt, nunca a senha em texto puro.
// Este record NUNCA é devolvido pela API (veja UsuarioResponse).
//
// A tabela usuario não tem coluna de perfil: o perfil é calculado pelo
// UsuarioRepository (ADMIN = e-mail configurado em app.admin.email, os demais USUARIO).
public record Usuario(
        Long id,
        String nome,
        String email,
        String senhaHash,
        Perfil perfil
) {
}
