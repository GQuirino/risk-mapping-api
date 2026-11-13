package com.riskmapping.controller;

import com.riskmapping.dto.ConfirmacaoRequest;
import com.riskmapping.dto.ResumoConfirmacoes;
import com.riskmapping.service.ConfirmacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Confirmações")
@RestController
@RequestMapping("/api/v1/riscos/{riscoId}/confirmacoes")
public class ConfirmacaoController {

    private final ConfirmacaoService service;

    public ConfirmacaoController(ConfirmacaoService service) {
        this.service = service;
    }

    // Quantos confirmam e quantos negam (público)
    @Operation(summary = "Quantos confirmam e quantos negam o risco")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping
    public ResumoConfirmacoes resumir(@PathVariable Long riscoId) {
        return service.resumir(riscoId);
    }

    // Registra (ou troca) o voto do usuário logado: CONFIRMA ou NEGA
    @Operation(summary = "Registra o voto do usuário no risco",
            description = "CONFIRMA (ainda existe) ou NEGA (não existe mais). Cada usuário tem um voto por risco: votar de novo troca o voto anterior.")
    @PutMapping
    public ResumoConfirmacoes votar(@PathVariable Long riscoId,
                                    @Valid @RequestBody ConfirmacaoRequest request) {
        return service.votar(riscoId, request.tipo());
    }

    // Retira o voto do usuário logado
    @Operation(summary = "Retira o voto do usuário no risco",
            description = "404 se o usuário ainda não votou.")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void removerMeuVoto(@PathVariable Long riscoId) {
        service.removerMeuVoto(riscoId);
    }
}
