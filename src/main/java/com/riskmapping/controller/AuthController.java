package com.riskmapping.controller;

import com.riskmapping.dto.LoginRequest;
import com.riskmapping.dto.RegistrarRequest;
import com.riskmapping.dto.TokenResponse;
import com.riskmapping.dto.UsuarioResponse;
import com.riskmapping.service.AuthService;
import com.riskmapping.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Autenticação")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public AuthController(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Cria uma conta (perfil USUARIO)")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED) // 201
    public UsuarioResponse registrar(@Valid @RequestBody RegistrarRequest request) {
        return usuarioService.registrar(request);
    }

    @Operation(summary = "Login: devolve o token JWT",
            description = "Execute, copie o valor de `token` da resposta, clique em **Authorize** "
                    + "(topo da página) e cole o token. Depois disso as rotas protegidas funcionam.")
    @SecurityRequirements // rota pública: sem cadeado no Swagger
    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletRequest http) {
        return authService.login(request, http.getRemoteAddr()); // IP usado no limite de tentativas
    }
}
