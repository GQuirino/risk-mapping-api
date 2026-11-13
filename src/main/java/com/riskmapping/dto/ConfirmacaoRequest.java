package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.riskmapping.model.TipoConfirmacao;
import jakarta.validation.constraints.NotNull;

public record ConfirmacaoRequest(

        @NotNull(message = "O tipo é obrigatório (CONFIRMA ou NEGA)")
        @Schema(description = "CONFIRMA = o risco ainda existe; NEGA = não existe mais", example = "CONFIRMA")
        TipoConfirmacao tipo
) {
}
