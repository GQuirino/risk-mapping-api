package com.riskmapping.exception;

// Vira HTTP 401 (e-mail ou senha errados)
public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
