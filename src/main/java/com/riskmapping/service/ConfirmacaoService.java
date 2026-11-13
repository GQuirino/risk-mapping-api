package com.riskmapping.service;

import com.riskmapping.dto.ResumoConfirmacoes;
import com.riskmapping.exception.RecursoNaoEncontradoException;
import com.riskmapping.model.TipoConfirmacao;
import com.riskmapping.repository.ConfirmacaoRepository;
import com.riskmapping.security.UsuarioLogado;
import org.springframework.stereotype.Service;

@Service
public class ConfirmacaoService {

    private final ConfirmacaoRepository confirmacaoRepository;
    private final RiscoService riscoService;
    private final UsuarioLogado usuarioLogado;

    public ConfirmacaoService(ConfirmacaoRepository confirmacaoRepository,
                              RiscoService riscoService,
                              UsuarioLogado usuarioLogado) {
        this.confirmacaoRepository = confirmacaoRepository;
        this.riscoService = riscoService;
        this.usuarioLogado = usuarioLogado;
    }

    public ResumoConfirmacoes resumir(Long riscoId) {
        riscoService.buscarPorId(riscoId); // 404 se o risco não existe
        return new ResumoConfirmacoes(
                confirmacaoRepository.contar(riscoId, TipoConfirmacao.CONFIRMA),
                confirmacaoRepository.contar(riscoId, TipoConfirmacao.NEGA));
    }

    // Cada usuário tem um voto por risco: votar de novo troca o voto anterior
    public ResumoConfirmacoes votar(Long riscoId, TipoConfirmacao tipo) {
        riscoService.buscarPorId(riscoId);
        confirmacaoRepository.salvar(usuarioLogado.id(), riscoId, tipo);
        return resumir(riscoId);
    }

    public void removerMeuVoto(Long riscoId) {
        riscoService.buscarPorId(riscoId);
        if (!confirmacaoRepository.remover(usuarioLogado.id(), riscoId)) {
            throw new RecursoNaoEncontradoException("Você ainda não votou neste risco");
        }
    }
}
