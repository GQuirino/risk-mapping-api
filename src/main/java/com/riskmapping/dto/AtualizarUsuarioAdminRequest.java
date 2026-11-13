package com.riskmapping.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Usado pelo ADMIN (PUT /usuarios/{id})
public record AtualizarUsuarioAdminRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 100, message = "O nome deve ter no máximo 100 caracteres")
        String nome,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "E-mail inválido")
        @Size(max = 120, message = "O e-mail deve ter no máximo 120 caracteres")
        String email
) {
}
