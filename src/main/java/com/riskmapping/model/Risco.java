package com.riskmapping.model;

import java.time.LocalDateTime;

// Um ponto de risco no mapa (tabela relato)
public record Risco(
        Long id,
        String titulo,
        String descricao,
        double latitude,
        double longitude,
        int gravidade, // 1 (leve) a 5 (gravíssimo)
        StatusRisco status,
        Long categoriaId,
        Long bairroId,
        Long criadoPorId, // id do usuário dono do risco
        LocalDateTime criadoEm
) {
}
