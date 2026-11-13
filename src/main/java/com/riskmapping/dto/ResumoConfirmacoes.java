package com.riskmapping.dto;

// Quantos usuários confirmam e quantos negam que o risco existe
public record ResumoConfirmacoes(int confirmacoes, int negacoes) {
}
