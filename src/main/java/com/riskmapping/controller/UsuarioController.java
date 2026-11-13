package com.riskmapping.controller;

import com.riskmapping.dto.AtualizarUsuarioAdminRequest;
import com.riskmapping.dto.AtualizarUsuarioRequest;
import com.riskmapping.dto.CriarUsuarioAdminRequest;
import com.riskmapping.dto.TrocarSenhaRequest;
import com.riskmapping.dto.UsuarioResponse;
import com.riskmapping.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Usuários")
@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    // ----- meu perfil (qualquer usuário logado) -----

    @Operation(summary = "Dados do usuário logado")
    @GetMapping("/me")
    public UsuarioResponse meusDados() {
        return service.buscarMeusDados();
    }

    @Operation(summary = "Atualiza nome e e-mail do usuário logado",
            description = "409 se o e-mail já é de outra conta. O ADMIN não pode trocar o e-mail.")
    @PutMapping("/me")
    public UsuarioResponse atualizarMeusDados(@Valid @RequestBody AtualizarUsuarioRequest request) {
        return service.atualizarMeusDados(request);
    }

    @Operation(summary = "Troca a senha do usuário logado",
            description = "Exige a senha atual (401 se estiver errada).")
    @PatchMapping("/me/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void trocarMinhaSenha(@Valid @RequestBody TrocarSenhaRequest request) {
        service.trocarMinhaSenha(request);
    }

    @Operation(summary = "Apaga a conta do usuário logado",
            description = "Apaga também os riscos, comentários e votos da conta. O token deixa de valer.")
    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void removerMinhaConta() {
        service.removerMinhaConta();
    }

    // ----- gerenciar usuários (só ADMIN) -----

    @Operation(summary = "Lista todos os usuários",
            description = "Só ADMIN.")
    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar();
    }

    @Operation(summary = "Busca um usuário pelo id",
            description = "Só ADMIN.")
    @GetMapping("/{id}")
    public UsuarioResponse buscarPorId(@PathVariable Long id) {
        return service.buscarResponsePorId(id);
    }

    @Operation(summary = "Cria um usuário",
            description = "Só ADMIN. A conta nasce como USUARIO: o único ADMIN é o e-mail de app.admin.email.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED) // 201
    public UsuarioResponse criar(@Valid @RequestBody CriarUsuarioAdminRequest request) {
        return service.criarComoAdmin(request);
    }

    @Operation(summary = "Atualiza nome e e-mail de um usuário",
            description = "Só ADMIN.")
    @PutMapping("/{id}")
    public UsuarioResponse atualizar(@PathVariable Long id,
                                     @Valid @RequestBody AtualizarUsuarioAdminRequest request) {
        return service.atualizarComoAdmin(id, request);
    }

    @Operation(summary = "Remove um usuário",
            description = "Só ADMIN. Apaga também os riscos, comentários e votos dele.")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT) // 204
    public void remover(@PathVariable Long id) {
        service.removerComoAdmin(id);
    }
}
