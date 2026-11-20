package com.riskmapping.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Usado pelo ADMIN (POST /usuarios): cria a conta como USUARIO
// (só o e-mail de app.admin.email é ADMIN, veja UsuarioRepository) 
public record CriarUsuarioAdminRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres")
        String senha,

        @NotBlank(message = "A confirmação da senha é obrigatória")
        @Schema(description = "Repita a senha")
        String confirmarSenha
) {

    // Erro 400 em "senhasIguais" quando a confirmação é diferente da senha
    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "As senhas não conferem")
    public boolean isSenhasIguais() {
        return senha == null || confirmarSenha == null || senha.equals(confirmarSenha);
    }
}
