package com.riskmapping.service;

import com.riskmapping.dto.AtualizarUsuarioAdminRequest;
import com.riskmapping.dto.AtualizarUsuarioRequest;
import com.riskmapping.dto.CriarUsuarioAdminRequest;
import com.riskmapping.dto.RegistrarRequest;
import com.riskmapping.dto.TrocarSenhaRequest;
import com.riskmapping.dto.UsuarioResponse;
import com.riskmapping.exception.ConflitoException;
import com.riskmapping.exception.CredenciaisInvalidasException;
import com.riskmapping.exception.RecursoNaoEncontradoException;
import com.riskmapping.model.Perfil;
import com.riskmapping.model.Usuario;
import com.riskmapping.repository.UsuarioRepository;
import com.riskmapping.security.UsuarioLogado;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioLogado usuarioLogado;
    private final String emailAdmin;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          UsuarioLogado usuarioLogado,
                          @Value("${app.admin.email}") String emailAdmin) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.usuarioLogado = usuarioLogado;
        this.emailAdmin = emailAdmin;
    }

    // ---------- cadastro (público) ----------

    public UsuarioResponse registrar(RegistrarRequest request) {
        validarEmailLivre(request.email());

        // quem se cadastra sozinho é sempre USUARIO; só um ADMIN vira ADMIN
        Usuario novo = new Usuario(null, request.nome(), request.email(),
                passwordEncoder.encode(request.senha()), Perfil.USUARIO);
        return UsuarioResponse.de(usuarioRepository.salvar(novo));
    }

    // ---------- "meu perfil" (usuário logado) ----------

    public UsuarioResponse buscarMeusDados() {
        return UsuarioResponse.de(buscarPorId(usuarioLogado.id()));
    }

    public UsuarioResponse atualizarMeusDados(AtualizarUsuarioRequest request) {
        Usuario atual = buscarPorId(usuarioLogado.id());
        validarEmailSeMudou(atual, request.email());

        Usuario atualizado = new Usuario(atual.id(), request.nome(), request.email(),
                atual.senhaHash(), atual.perfil());
        return UsuarioResponse.de(usuarioRepository.salvar(atualizado));
    }

    public void trocarMinhaSenha(TrocarSenhaRequest request) {
        Usuario atual = buscarPorId(usuarioLogado.id());

        if (!passwordEncoder.matches(request.senhaAtual(), atual.senhaHash())) {
            throw new CredenciaisInvalidasException("Senha atual incorreta");
        }

        Usuario atualizado = new Usuario(atual.id(), atual.nome(), atual.email(),
                passwordEncoder.encode(request.novaSenha()), atual.perfil());
        usuarioRepository.salvar(atualizado);
    }

    public void removerMinhaConta() {
        usuarioRepository.remover(usuarioLogado.id());
    }

    // ---------- gerenciar usuários (só ADMIN, garantido no SecurityConfig) ----------

    public List<UsuarioResponse> listar() {
        return usuarioRepository.listar().stream().map(UsuarioResponse::de).toList();
    }

    public UsuarioResponse buscarResponsePorId(Long id) {
        return UsuarioResponse.de(buscarPorId(id));
    }

    public UsuarioResponse criarComoAdmin(CriarUsuarioAdminRequest request) {
        validarEmailLivre(request.email());

        Usuario novo = new Usuario(null, request.nome(), request.email(),
                passwordEncoder.encode(request.senha()), Perfil.USUARIO);
        return UsuarioResponse.de(usuarioRepository.salvar(novo));
    }

    public UsuarioResponse atualizarComoAdmin(Long id, AtualizarUsuarioAdminRequest request) {
        Usuario atual = buscarPorId(id);
        validarEmailSeMudou(atual, request.email());

        Usuario atualizado = new Usuario(id, request.nome(), request.email(),
                atual.senhaHash(), atual.perfil());
        return UsuarioResponse.de(usuarioRepository.salvar(atualizado));
    }

    public void removerComoAdmin(Long id) {
        buscarPorId(id); // 404 se não existir
        usuarioRepository.remover(id);
    }

    // ---------- auxiliares ----------

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário " + id + " não encontrado"));
    }

    // O e-mail do ADMIN é reservado: o perfil ADMIN é decidido só por ele (veja UsuarioRepository),
    // então ninguém pode se cadastrar com ele nem trocar para ele, mesmo que a conta tenha sumido.
    private void validarEmailLivre(String email) {
        if (usuarioRepository.existePorEmail(email) || email.equalsIgnoreCase(emailAdmin)) {
            throw new ConflitoException("Já existe um usuário com o e-mail " + email);
        }
    }

    private void validarEmailSeMudou(Usuario atual, String novoEmail) {
        boolean mudou = !atual.email().equalsIgnoreCase(novoEmail);
        if (mudou && atual.perfil() == Perfil.ADMIN) {
            // sem a coluna de perfil, mudar o e-mail do ADMIN tiraria o acesso dele
            throw new ConflitoException("O e-mail do administrador só muda em app.admin.email");
        }
        if (mudou) {
            validarEmailLivre(novoEmail);
        }
    }
}
