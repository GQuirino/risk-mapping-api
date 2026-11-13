package com.riskmapping.repository;

import com.riskmapping.model.Comentario;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

// Tabela comentario
@Repository
public class ComentarioRepository {

    private static final String COLUNAS = "id_comentario, id_relato, id_usuario, texto, data_criacao";

    private final JdbcClient jdbc;

    public ComentarioRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public List<Comentario> listarPorRisco(Long riscoId) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM comentario WHERE id_relato = :id "
                        + "ORDER BY data_criacao, id_comentario")
                .param("id", riscoId)
                .query(this::mapear).list();
    }

    public Optional<Comentario> buscarPorId(Long id) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM comentario WHERE id_comentario = :id")
                .param("id", id)
                .query(this::mapear).optional();
    }

    // Comentários não são editados: só criados e removidos
    public Comentario criar(Long riscoId, Long usuarioId, String texto) {
        KeyHolder chave = new GeneratedKeyHolder();
        jdbc.sql("INSERT INTO comentario (id_relato, id_usuario, texto) "
                        + "VALUES (:risco, :usuario, :texto)")
                .param("risco", riscoId)
                .param("usuario", usuarioId)
                .param("texto", texto)
                .update(chave);
        return buscarPorId(chave.getKey().longValue()).orElseThrow();
    }

    public void remover(Long id) {
        jdbc.sql("DELETE FROM comentario WHERE id_comentario = :id").param("id", id).update();
    }

    private Comentario mapear(ResultSet rs, int linha) throws SQLException {
        return new Comentario(rs.getLong("id_comentario"), rs.getLong("id_relato"),
                rs.getLong("id_usuario"), rs.getString("texto"),
                rs.getObject("data_criacao", LocalDateTime.class));
    }
}
