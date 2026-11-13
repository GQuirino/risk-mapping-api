package com.riskmapping.service;

import com.riskmapping.dto.BairroRequest;
import com.riskmapping.exception.ConflitoException;
import com.riskmapping.exception.RecursoNaoEncontradoException;
import com.riskmapping.model.Bairro;
import com.riskmapping.repository.BairroRepository;
import com.riskmapping.repository.RiscoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BairroService {

    private final BairroRepository bairroRepository;
    private final RiscoRepository riscoRepository;

    public BairroService(BairroRepository bairroRepository, RiscoRepository riscoRepository) {
        this.bairroRepository = bairroRepository;
        this.riscoRepository = riscoRepository;
    }

    public List<Bairro> listar() {
        return bairroRepository.listar();
    }

    public Bairro buscarPorId(Long id) {
        return bairroRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bairro " + id + " não encontrado"));
    }

    public Bairro criar(BairroRequest request) {
        String estado = request.estado().toUpperCase();
        exigirLocalLivre(null, request.nome(), request.cidade(), estado);
        return bairroRepository.salvar(new Bairro(null, request.nome(), request.cidade(), estado));
    }

    public Bairro atualizar(Long id, BairroRequest request) {
        buscarPorId(id);
        String estado = request.estado().toUpperCase();
        exigirLocalLivre(id, request.nome(), request.cidade(), estado);
        return bairroRepository.salvar(new Bairro(id, request.nome(), request.cidade(), estado));
    }

    public void remover(Long id) {
        buscarPorId(id);

        if (riscoRepository.existePorBairro(id)) {
            throw new ConflitoException("Bairro em uso por riscos, não pode ser removido");
        }
        bairroRepository.remover(id);
    }

    // Conflito se outro bairro (diferente de idAtual) já tem o mesmo nome + cidade + estado
    private void exigirLocalLivre(Long idAtual, String nome, String cidade, String estado) {
        bairroRepository.buscarPorLocal(nome, cidade, estado)
                .filter(outro -> !outro.id().equals(idAtual))
                .ifPresent(outro -> {
                    throw new ConflitoException("Já existe o bairro " + nome + " em " + cidade + "/" + estado);
                });
    }
}
