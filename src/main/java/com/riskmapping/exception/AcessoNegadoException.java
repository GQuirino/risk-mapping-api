package com.riskmapping.exception;

// Vira HTTP 403 (logado, mas sem permissão para aquele recurso)
public class AcessoNegadoException extends RuntimeException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
