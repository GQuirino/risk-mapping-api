package com.riskmapping.repository;

import com.riskmapping.model.Categoria;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Tabela categoria_risco
@Repository
public class CategoriaRepository {

    private static final String COLUNAS = "id_categoria, nome, descricao";

    private final JdbcClient jdbc;

    public CategoriaRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Categoria> listar() {
        return jdbc.sql("SELECT " + COLUNAS + " FROM categoria_risco ORDER BY id_categoria")
                .query(this::mapear).list();
    }

    public Optional<Categoria> buscarPorId(Long id) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM categoria_risco WHERE id_categoria = :id")
                .param("id", id)
                .query(this::mapear).optional();
    }

    public boolean existePorNome(String nome) {
        return jdbc.sql("SELECT COUNT(*) > 0 FROM categoria_risco WHERE nome = :nome")
                .param("nome", nome)
                .query(Boolean.class).single();
    }

    public Categoria salvar(Categoria categoria) {
        Long id = categoria.id();
        if (id == null) {
            KeyHolder chave = new GeneratedKeyHolder();
            jdbc.sql("INSERT INTO categoria_risco (nome, descricao) VALUES (:nome, :descricao)")
                    .param("nome", categoria.nome())
                    .param("descricao", categoria.descricao())
                    .update(chave);
            id = chave.getKey().longValue();
        } else {
            jdbc.sql("UPDATE categoria_risco SET nome = :nome, descricao = :descricao "
                            + "WHERE id_categoria = :id")
                    .param("nome", categoria.nome())
                    .param("descricao", categoria.descricao())
                    .param("id", id)
                    .update();
        }
        return buscarPorId(id).orElseThrow();
    }

    public void remover(Long id) {
        jdbc.sql("DELETE FROM categoria_risco WHERE id_categoria = :id").param("id", id).update();
    }

    private Categoria mapear(ResultSet rs, int linha) throws SQLException {
        return new Categoria(rs.getLong("id_categoria"), rs.getString("nome"),
                rs.getString("descricao"));
    }
}
