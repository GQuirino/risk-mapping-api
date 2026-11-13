package com.riskmapping.controller;

import com.riskmapping.dto.ComentarioRequest;
import com.riskmapping.model.Comentario;
import com.riskmapping.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Comentários")
@RestController
@RequestMapping("/api/v1")
public class ComentarioController {

    private final ComentarioService service;

    public ComentarioController(ComentarioService service) {
        this.service = service;
    }

    // Listar e criar ficam dentro de um risco
    @Operation(summary = "Lista os comentários de um risco",
            description = "Do mais antigo para o mais novo.")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @GetMapping("/riscos/{riscoId}/comentarios")
    public List<Comentario> listarPorRisco(@PathVariable Long riscoId) {
        return service.listarPorRisco(riscoId);
    }

    @Operation(summary = "Comenta em um risco",
            description = "Qualquer usuário logado.")
    @PostMapping("/riscos/{riscoId}/comentarios")
    @ResponseStatus(HttpStatus.CREATED) // 201
    public Comentario criar(@PathVariable Long riscoId, @Valid @RequestBody ComentarioRequest request) {
        return service.criar(riscoId, request);
    }

    // Remover usa só o id do comentário
    @Operation(summary = "Remove um comentário",
            description = "Só o autor do comentário ou um ADMIN (403).")
    @DeleteMapping("/comentarios/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void remover(@PathVariable Long id) {
        service.remover(id);
    }
}
