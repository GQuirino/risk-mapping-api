package com.riskmapping.controller;

import com.riskmapping.dto.CategoriaRequest;
import com.riskmapping.model.Categoria;
import com.riskmapping.service.CategoriaService;
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

@Tag(name = "Categorias")
@RestController
@RequestMapping("/api/v1/categorias")
public class CategoriaController {

    private final CategoriaService service;

    public CategoriaController(CategoriaService service) {
        this.service = service;
    }

    @Operation(summary = "Lista as categorias de risco")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping
    public List<Categoria> listar() {
        return service.listar();
    }

    @Operation(summary = "Busca uma categoria pelo id")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping("/{id}")
    public Categoria buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id);
    }

    @Operation(summary = "Cria uma categoria",
            description = "Só ADMIN. 409 se o nome já existe.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    public Categoria criar(@Valid @RequestBody CategoriaRequest request) {
        return service.criar(request);
    }

    @Operation(summary = "Atualiza uma categoria",
            description = "Só ADMIN.")
    @PutMapping("/{id}")
    public Categoria atualizar(@PathVariable Long id, @Valid @RequestBody CategoriaRequest request) {
        return service.atualizar(id, request);
    }

    @Operation(summary = "Remove uma categoria",
            description = "Só ADMIN. 409 se houver riscos usando a categoria.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
