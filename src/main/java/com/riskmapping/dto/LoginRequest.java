package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatório")
        @Schema(example = "ana@email.com")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        @Schema(example = "minha-senha-123")
        String senha
) {
}
