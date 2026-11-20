package com.riskmapping.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegistrarRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        @Schema(example = "Ana Souza")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
        @Schema(example = "ana@email.com")
        String email,

        // BCrypt só considera os 72 primeiros bytes, por isso o limite
        @NotBlank(message = "A senha é obrigatória")
        @Size(min = 8, max = 72, message = "A senha deve ter de 8 a 72 caracteres")
        @Schema(description = "De 8 a 72 caracteres", example = "minha-senha-123")
        String senha,

        @NotBlank(message = "A confirmação da senha é obrigatória")
        @Schema(description = "Repita a senha", example = "minha-senha-123")
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
