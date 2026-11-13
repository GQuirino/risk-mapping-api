package com.riskmapping.model;

// Bairro onde o risco acontece (tabela bairro)
public record Bairro(
        Long id,
        String nome,
        String cidade,
        String estado // sigla da UF, ex: SP
) {
}
