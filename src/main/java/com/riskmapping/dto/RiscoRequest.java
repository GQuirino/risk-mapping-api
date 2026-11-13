package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RiscoRequest(

        @NotBlank(message = "O título é obrigatório")
        @Size(max = 120, message = "O título deve ter no máximo 120 caracteres")
        @Schema(example = "Buraco grande na avenida")
        String titulo,

        @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
        @Schema(example = "Buraco na faixa da direita, perigoso para motos.")
        String descricao,

        @NotNull(message = "A latitude é obrigatória")
        @DecimalMin(value = "-90", message = "A latitude mínima é -90")
        @DecimalMax(value = "90", message = "A latitude máxima é 90")
        @Schema(example = "-23.566")
        Double latitude,

        @NotNull(message = "A longitude é obrigatória")
        @DecimalMin(value = "-180", message = "A longitude mínima é -180")
        @DecimalMax(value = "180", message = "A longitude máxima é 180")
        @Schema(example = "-46.693")
        Double longitude,

        @NotNull(message = "A gravidade é obrigatória")
        @Min(value = 1, message = "A gravidade mínima é 1")
        @Max(value = 5, message = "A gravidade máxima é 5")
        @Schema(description = "De 1 (leve) a 5 (gravíssimo)", example = "3")
        Integer gravidade,

        @NotNull(message = "A categoria é obrigatória")
        @Schema(description = "Id de GET /categorias", example = "3")
        Long categoriaId,

        @NotNull(message = "O bairro é obrigatório")
        @Schema(description = "Id de GET /bairros", example = "1")
        Long bairroId
) {
}
