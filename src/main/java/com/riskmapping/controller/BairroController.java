package com.riskmapping.controller;

import com.riskmapping.dto.BairroRequest;
import com.riskmapping.model.Bairro;
import com.riskmapping.service.BairroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Bairros")
@RestController
@RequestMapping("/api/v1/bairros")
public class BairroController {

    private final BairroService service;

    public BairroController(BairroService service) {
        this.service = service;
    }

    @Operation(summary = "Lista os bairros")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping
    public List<Bairro> listar() {
        return service.listar();
    }

    @Operation(summary = "Busca um bairro pelo id")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping("/{id}")
    public Bairro buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Cria um bairro",
            description = "Só ADMIN. A sigla do estado é gravada em maiúsculas. 409 se já existe o mesmo nome na mesma cidade/estado.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    public Bairro criar(@Valid @RequestBody BairroRequest request) {
        return service.criar(request);
    }

    @Operation(summary = "Atualiza um bairro",
            description = "Só ADMIN.")
    @PutMapping("/{id}")
    public Bairro atualizar(@PathVariable Long id, @Valid @RequestBody BairroRequest request) {
        return service.atualizar(id, request);
    }

    @Operation(summary = "Remove um bairro",
            description = "Só ADMIN. 409 se houver riscos nesse bairro.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
