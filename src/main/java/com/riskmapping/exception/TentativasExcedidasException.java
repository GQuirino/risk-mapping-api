package com.riskmapping.exception;

// Vira HTTP 429 (muitas tentativas de login seguidas)
public class TentativasExcedidasException extends RuntimeException {

    public TentativasExcedidasException(String mensagem) {
        super(mensagem);
    }
}
