package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BairroRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
        @Schema(example = "Pinheiros")
        String nome,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 80, message = "A cidade deve ter no máximo 80 caracteres")
        @Schema(example = "São Paulo")
        String cidade,

        @NotBlank(message = "O estado é obrigatório")
        @Pattern(regexp = "^[A-Za-z]{2}$", message = "O estado deve ser a sigla da UF, ex: SP")
        @Schema(description = "Sigla da UF", example = "SP")
        String estado
) {
}
