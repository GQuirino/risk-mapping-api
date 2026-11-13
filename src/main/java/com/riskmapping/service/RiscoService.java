package com.riskmapping.service;

import com.riskmapping.dto.RiscoRequest;
import com.riskmapping.exception.RecursoNaoEncontradoException;
import com.riskmapping.model.Risco;
import com.riskmapping.model.StatusRisco;
import com.riskmapping.repository.RiscoRepository;
import com.riskmapping.security.UsuarioLogado;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RiscoService {

    private final RiscoRepository riscoRepository;
    private final CategoriaService categoriaService;
    private final BairroService bairroService;
    private final UsuarioLogado usuarioLogado;

    public RiscoService(RiscoRepository riscoRepository,
                        CategoriaService categoriaService,
                        BairroService bairroService,
                        UsuarioLogado usuarioLogado) {
        this.riscoRepository = riscoRepository;
        this.categoriaService = categoriaService;
        this.bairroService = bairroService;
        this.usuarioLogado = usuarioLogado;
    }

    // Só o dono do risco ou um ADMIN pode alterar.
    public Risco buscarParaAlterar(Long id) {
        Risco risco = buscarPorId(id);
        usuarioLogado.exigirDonoOuAdmin(risco.criadoPorId());
        return risco;
    }

    // Todos os filtros são opcionais: o que vier null é ignorado
    public List<Risco> listar(Long categoriaId, Long bairroId, Integer gravidade,
                              StatusRisco status, String busca) {
        return riscoRepository.listar(categoriaId, bairroId, gravidade, status, busca);
    }

    public Risco buscarPorId(Long id) {
        return riscoRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Risco " + id + " não encontrado"));
    }

    public Risco criar(RiscoRequest request) {
        categoriaService.buscarPorId(request.categoriaId()); // 404 se a categoria não existe
        bairroService.buscarPorId(request.bairroId());       // 404 se o bairro não existe

        Risco novo = new Risco(null, request.titulo(), request.descricao(),
                request.latitude(), request.longitude(), request.gravidade(),
                StatusRisco.ABERTO, request.categoriaId(), request.bairroId(),
                usuarioLogado.id(), null);
        return riscoRepository.salvar(novo);
    }

    public Risco atualizar(Long id, RiscoRequest request) {
        Risco existente = buscarParaAlterar(id);
        categoriaService.buscarPorId(request.categoriaId());
        bairroService.buscarPorId(request.bairroId());

        Risco atualizado = new Risco(id, request.titulo(), request.descricao(),
                request.latitude(), request.longitude(), request.gravidade(),
                existente.status(), request.categoriaId(), request.bairroId(),
                existente.criadoPorId(), existente.criadoEm());
        return riscoRepository.salvar(atualizado);
    }

    public Risco mudarStatus(Long id, StatusRisco novoStatus) {
        Risco existente = buscarParaAlterar(id);

        Risco atualizado = new Risco(id, existente.titulo(), existente.descricao(),
                existente.latitude(), existente.longitude(), existente.gravidade(),
                novoStatus, existente.categoriaId(), existente.bairroId(),
                existente.criadoPorId(), existente.criadoEm());
        return riscoRepository.salvar(atualizado);
    }

    public void remover(Long id) {
        buscarParaAlterar(id);
        riscoRepository.remover(id); // comentários e confirmações saem junto (CASCADE)
    }
}
