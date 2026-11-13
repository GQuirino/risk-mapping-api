package com.riskmapping.controller;

import com.riskmapping.dto.RiscoRequest;
import com.riskmapping.dto.StatusRequest;
import com.riskmapping.model.Risco;
import com.riskmapping.model.StatusRisco;
import com.riskmapping.service.RiscoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Riscos")
@RestController
@RequestMapping("/api/v1/riscos")
public class RiscoController {

    private final RiscoService service;

    public RiscoController(RiscoService service) {
        this.service = service;
    }

    // Filtros opcionais: /riscos?categoriaId=1&bairroId=3&gravidade=4&status=ABERTO&busca=rio
    @Operation(summary = "Lista os riscos do mapa",
            description = "Filtros opcionais, combináveis: categoriaId, bairroId, gravidade (1 a 5), status e busca (no título ou na descrição).")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping
    public List<Risco> listar(@RequestParam(required = false) Long categoriaId,
                              @RequestParam(required = false) Long bairroId,
                              @RequestParam(required = false) Integer gravidade,
                              @RequestParam(required = false) StatusRisco status,
                              @RequestParam(required = false) String busca) {
        return service.listar(categoriaId, bairroId, gravidade, status, busca);
    }

    @Operation(summary = "Busca um risco pelo id")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping("/{id}")
    public Risco buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Cria um risco",
            description = "Qualquer usuário logado. Nasce com status ABERTO e o dono é o usuário do token. categoriaId e bairroId precisam existir (404).")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    public Risco criar(@Valid @RequestBody RiscoRequest request) {
        return service.criar(request);
    }

    @Operation(summary = "Atualiza um risco",
            description = "Só o dono do risco ou um ADMIN (403). Não muda o status: use PATCH /status.")
    @PutMapping("/{id}")
    public Risco atualizar(@PathVariable Long id, @Valid @RequestBody RiscoRequest request) {
        return service.atualizar(id, request);
    }

    // Muda só o status (ABERTO, CONFIRMADO, RESOLVIDO)
    @Operation(summary = "Muda o status do risco",
            description = "Valores: ABERTO, CONFIRMADO, RESOLVIDO. Só o dono ou um ADMIN.")
    @PatchMapping("/{id}/status")
    public Risco mudarStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return service.mudarStatus(id, request.status());
    }

    @Operation(summary = "Remove um risco",
            description = "Só o dono ou um ADMIN. Remove também os comentários e as confirmações do risco.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
