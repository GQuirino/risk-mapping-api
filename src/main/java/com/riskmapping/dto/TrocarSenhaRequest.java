package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrocarSenhaRequest(

        @NotBlank(message = "A senha atual é obrigatória")
        @Schema(example = "minha-senha-123")
        String senhaAtual,

        @NotBlank(message = "A nova senha é obrigatória")
        @Size(min = 8, max = 72, message = "A nova senha deve ter de 8 a 72 caracteres")
        @Schema(description = "De 8 a 72 caracteres", example = "outra-senha-456")
        String novaSenha
) {
}
