package com.riskmapping.repository;

import com.riskmapping.model.Bairro;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Tabela bairro
@Repository
public class BairroRepository {

    private static final String COLUNAS = "id_bairro, nome, cidade, estado";

    private final JdbcClient jdbc;

    public BairroRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Bairro> listar() {
        return jdbc.sql("SELECT " + COLUNAS + " FROM bairro ORDER BY estado, cidade, nome")
                .query(this::mapear).list();
    }

    public Optional<Bairro> buscarPorId(Long id) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM bairro WHERE id_bairro = :id")
                .param("id", id)
                .query(this::mapear).optional();
    }

    // Mesma regra do UNIQUE uq_bairro (nome + cidade + estado)
    public Optional<Bairro> buscarPorLocal(String nome, String cidade, String estado) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM bairro "
                        + "WHERE nome = :nome AND cidade = :cidade AND estado = :estado")
                .param("nome", nome)
                .param("cidade", cidade)
                .param("estado", estado)
                .query(this::mapear).optional();
    }

    public Bairro salvar(Bairro bairro) {
        Long id = bairro.id();
        if (id == null) {
            KeyHolder chave = new GeneratedKeyHolder();
            jdbc.sql("INSERT INTO bairro (nome, cidade, estado) VALUES (:nome, :cidade, :estado)")
                    .param("nome", bairro.nome())
                    .param("cidade", bairro.cidade())
                    .param("estado", bairro.estado())
                    .update(chave);
            id = chave.getKey().longValue();
        } else {
            jdbc.sql("UPDATE bairro SET nome = :nome, cidade = :cidade, estado = :estado "
                            + "WHERE id_bairro = :id")
                    .param("nome", bairro.nome())
                    .param("cidade", bairro.cidade())
                    .param("estado", bairro.estado())
                    .param("id", id)
                    .update();
        }
        return buscarPorId(id).orElseThrow();
    }

    public void remover(Long id) {
        jdbc.sql("DELETE FROM bairro WHERE id_bairro = :id").param("id", id).update();
    }

    private Bairro mapear(ResultSet rs, int linha) throws SQLException {
        return new Bairro(rs.getLong("id_bairro"), rs.getString("nome"),
                rs.getString("cidade"), rs.getString("estado"));
    }
}
