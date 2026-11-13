package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComentarioRequest(

        @NotBlank(message = "O texto é obrigatório")
        @Size(max = 500, message = "O texto deve ter no máximo 500 caracteres")
        @Schema(example = "Passei por aqui agora e continua alagado.")
        String texto
) {
}
