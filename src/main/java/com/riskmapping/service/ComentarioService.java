package com.riskmapping.service;

import com.riskmapping.dto.ComentarioRequest;
import com.riskmapping.exception.RecursoNaoEncontradoException;
import com.riskmapping.model.Comentario;
import com.riskmapping.repository.ComentarioRepository;
import com.riskmapping.security.UsuarioLogado;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final RiscoService riscoService;
    private final UsuarioLogado usuarioLogado;

    public ComentarioService(ComentarioRepository comentarioRepository,
                             RiscoService riscoService,
                             UsuarioLogado usuarioLogado) {
        this.comentarioRepository = comentarioRepository;
        this.riscoService = riscoService;
        this.usuarioLogado = usuarioLogado;
    }

    public List<Comentario> listarPorRisco(Long riscoId) {
        riscoService.buscarPorId(riscoId); // 404 se o risco não existe
        return comentarioRepository.listarPorRisco(riscoId);
    }

    // Qualquer usuário logado pode comentar
    public Comentario criar(Long riscoId, ComentarioRequest request) {
        riscoService.buscarPorId(riscoId);
        return comentarioRepository.criar(riscoId, usuarioLogado.id(), request.texto());
    }

    // Só o autor do comentário ou um ADMIN
    public void remover(Long id) {
        Comentario comentario = comentarioRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Comentário " + id + " não encontrado"));
        usuarioLogado.exigirDonoOuAdmin(comentario.usuarioId());
        comentarioRepository.remover(id);
    }
}
