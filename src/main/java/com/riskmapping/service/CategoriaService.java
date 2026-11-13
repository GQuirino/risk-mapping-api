package com.riskmapping.service;

import com.riskmapping.dto.CategoriaRequest;
import com.riskmapping.exception.ConflitoException;
import com.riskmapping.exception.RecursoNaoEncontradoException;
import com.riskmapping.model.Categoria;
import com.riskmapping.repository.CategoriaRepository;
import com.riskmapping.repository.RiscoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final RiscoRepository riscoRepository;

    public CategoriaService(CategoriaRepository categoriaRepository, RiscoRepository riscoRepository) {
        this.categoriaRepository = categoriaRepository;
        this.riscoRepository = riscoRepository;
    }

    public List<Categoria> listar() {
        return categoriaRepository.listar();
    }

    public Categoria buscarPorId(Long id) {
        return categoriaRepository.buscarPorId(id)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Categoria " + id + " não encontrada"));
    }

    public Categoria criar(CategoriaRequest request) {
        if (categoriaRepository.existePorNome(request.nome())) {
            throw new ConflitoException("Já existe uma categoria com o nome " + request.nome());
        }
        return categoriaRepository.salvar(new Categoria(null, request.nome(), request.descricao()));
    }

    public Categoria atualizar(Long id, CategoriaRequest request) {
        Categoria existente = buscarPorId(id);

        // só dá conflito se o nome mudou e já está em uso por outra categoria
        boolean mudouNome = !existente.nome().equalsIgnoreCase(request.nome());
        if (mudouNome && categoriaRepository.existePorNome(request.nome())) {
            throw new ConflitoException("Já existe uma categoria com o nome " + request.nome());
        }

        return categoriaRepository.salvar(new Categoria(id, request.nome(), request.descricao()));
    }

    public void remover(Long id) {
        buscarPorId(id); // garante o 404 se não existir

        if (riscoRepository.existePorCategoria(id)) {
            throw new ConflitoException("Categoria em uso por riscos, não pode ser removida");
        }
        categoriaRepository.remover(id);
    }
}
