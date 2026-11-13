package com.riskmapping.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import com.riskmapping.model.StatusRisco;
import jakarta.validation.constraints.NotNull;

public record StatusRequest(

        @NotNull(message = "O status é obrigatório")
        @Schema(description = "ABERTO, CONFIRMADO ou RESOLVIDO", example = "RESOLVIDO")
        StatusRisco status
) {
}
