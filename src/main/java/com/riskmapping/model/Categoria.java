package com.riskmapping.model;

// Tipo de risco (tabela categoria_risco): alagamento, deslizamento, buraco...
public record Categoria(
        Long id,
        String nome,
        String descricao
) {
}
