package com.riskmapping.model;

import java.time.LocalDateTime;

// Comentário de um usuário em um risco (tabela comentario)
public record Comentario(
        Long id,
        Long riscoId,
        Long usuarioId,
        String texto,
        LocalDateTime criadoEm
) {
}
