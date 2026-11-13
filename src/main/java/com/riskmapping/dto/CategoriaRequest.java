package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoriaRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
        @Schema(example = "Alagamento")
        String nome,

        @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
        @Schema(example = "Acúmulo de água em ruas e avenidas")
        String descricao
) {
}
