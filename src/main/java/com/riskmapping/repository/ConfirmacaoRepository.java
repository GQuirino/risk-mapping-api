package com.riskmapping.repository;

import com.riskmapping.model.TipoConfirmacao;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

// Tabela confirmacao (chave primária: usuário + risco)
@Repository
public class ConfirmacaoRepository {

    private final JdbcClient jdbc;

    public ConfirmacaoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Um voto por usuário em cada risco: votar de novo troca o tipo
    public void salvar(Long usuarioId, Long riscoId, TipoConfirmacao tipo) {
        jdbc.sql("INSERT INTO confirmacao (id_usuario, id_relato, tipo) "
                        + "VALUES (:usuario, :risco, :tipo) "
                        + "ON DUPLICATE KEY UPDATE tipo = :tipo")
                .param("usuario", usuarioId)
                .param("risco", riscoId)
                .param("tipo", tipo.name())
                .update();
    }

    public int contar(Long riscoId, TipoConfirmacao tipo) {
        return jdbc.sql("SELECT COUNT(*) FROM confirmacao WHERE id_relato = :risco AND tipo = :tipo")
                .param("risco", riscoId)
                .param("tipo", tipo.name())
                .query(Integer.class).single();
    }

    // Devolve true se havia um voto para remover
    public boolean remover(Long usuarioId, Long riscoId) {
        return jdbc.sql("DELETE FROM confirmacao WHERE id_usuario = :usuario AND id_relato = :risco")
                .param("usuario", usuarioId)
                .param("risco", riscoId)
                .update() > 0;
    }
}
