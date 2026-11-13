package com.riskmapping.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// Converte as exceções em respostas HTTP padronizadas
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflitoException.class)
    public ProblemDetail conflito(ConflitoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(TentativasExcedidasException.class)
    public ResponseEntity<ProblemDetail> tentativasExcedidas(TentativasExcedidasException e) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.TOO_MANY_REQUESTS, e.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "900") // segundos (a janela é de 15 minutos)
                .body(problema);
    }

    @ExceptionHandler(AcessoNegadoException.class)
    public ProblemDetail acessoNegado(AcessoNegadoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ProblemDetail credenciaisInvalidas(CredenciaisInvalidasException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    // Erros do @Valid: devolve um mapa campo -> mensagem
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacao(MethodArgumentNotValidException e) {
        Map<String, String> erros = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
                .forEach(erro -> erros.put(erro.getField(), erro.getDefaultMessage()));

        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "Dados inválidos");
        problema.setProperty("erros", erros);
        return problema;
    }

    // Corrida entre duas requisições (ex: dois cadastros com o mesmo e-mail ao mesmo tempo):
    // o banco recusa pelo UNIQUE/FOREIGN KEY mesmo que a checagem do service tenha passado
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridade(DataIntegrityViolationException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "A operação viola uma regra de integridade dos dados (registro duplicado ou em uso)");
    }

    // JSON quebrado ou valor de enum inválido (ex: status "XYZ")
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail jsonInvalido(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST, "JSON inválido ou valor não reconhecido");
    }
}
