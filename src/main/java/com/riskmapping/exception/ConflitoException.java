package com.riskmapping.exception;

// Vira HTTP 409 (ex: nome duplicado, categoria em uso)
public class ConflitoException extends RuntimeException {

    public ConflitoException(String mensagem) {
        super(mensagem);
    }
}
