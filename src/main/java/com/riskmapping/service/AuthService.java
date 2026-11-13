package com.riskmapping.service;

import com.riskmapping.dto.LoginRequest;
import com.riskmapping.dto.TokenResponse;
import com.riskmapping.exception.CredenciaisInvalidasException;
import com.riskmapping.model.Usuario;
import com.riskmapping.repository.UsuarioRepository;
import com.riskmapping.security.JwtService;
import com.riskmapping.security.LimiteTentativasLogin;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LimiteTentativasLogin limiteTentativas;

    // Hash de uma senha qualquer, usado quando o e-mail não existe (veja o login)
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       LimiteTentativasLogin limiteTentativas) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.limiteTentativas = limiteTentativas;
        this.hashFalso = passwordEncoder.encode("senha-que-ninguem-usa");
    }

    public TokenResponse login(LoginRequest request, String ip) {
        limiteTentativas.exigirNaoBloqueado(ip, request.email()); // 429 se passou do limite

        Optional<Usuario> usuario = usuarioRepository.buscarPorEmail(request.email());

        // O BCrypt (lento) roda SEMPRE, mesmo quando o e-mail não existe. Sem isso, e-mail
        // inexistente respondia em ~5 ms e e-mail existente em ~100 ms, e o tempo de
        // resposta revelava quais e-mails têm conta.
        String hash = usuario.map(Usuario::senhaHash).orElse(hashFalso);
        boolean senhaCorreta = passwordEncoder.matches(request.senha(), hash);

        // mesma mensagem para e-mail inexistente e senha errada (não revela qual deu erro)
        if (usuario.isEmpty() || !senhaCorreta) {
            limiteTentativas.registrarFalha(ip, request.email());
            throw new CredenciaisInvalidasException("E-mail ou senha inválidos");
        }

        limiteTentativas.registrarSucesso(ip, request.email());
        return new TokenResponse(jwtService.gerar(usuario.get()));
    }
}
