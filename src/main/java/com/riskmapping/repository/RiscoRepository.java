package com.riskmapping.repository;

import com.riskmapping.model.Risco;
import com.riskmapping.model.StatusRisco;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

// Tabela relato
@Repository
public class RiscoRepository {

    private static final String COLUNAS = "id_relato, titulo, descricao, latitude, longitude, "
            + "gravidade, status, id_categoria, id_bairro, id_usuario, data_criacao";

    private final JdbcClient jdbc;

    public RiscoRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    // Todos os filtros são opcionais: o que vier null é ignorado
    public List<Risco> listar(Long categoriaId, Long bairroId, Integer gravidade,
                              StatusRisco status, String busca) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUNAS + " FROM relato WHERE 1 = 1");
        Map<String, Object> params = new HashMap<>();

        if (categoriaId != null) {
            sql.append(" AND id_categoria = :categoriaId");
            params.put("categoriaId", categoriaId);
        }
        if (bairroId != null) {
            sql.append(" AND id_bairro = :bairroId");
            params.put("bairroId", bairroId);
        }
        if (gravidade != null) {
            sql.append(" AND gravidade = :gravidade");
            params.put("gravidade", gravidade);
        }
        if (status != null) {
            sql.append(" AND status = :status");
            params.put("status", status.name());
        }
        if (busca != null && !busca.isBlank()) {
            // escapa % _ \ para a busca ser literal (a collation já ignora maiúsculas)
            String literal = busca.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            sql.append(" AND (titulo LIKE :busca OR descricao LIKE :busca)");
            params.put("busca", "%" + literal + "%");
        }
        sql.append(" ORDER BY id_relato");

        return jdbc.sql(sql.toString()).params(params).query(this::mapear).list();
    }

    public Optional<Risco> buscarPorId(Long id) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM relato WHERE id_relato = :id")
                .param("id", id)
                .query(this::mapear).optional();
    }

    public boolean existePorCategoria(Long categoriaId) {
        return jdbc.sql("SELECT COUNT(*) > 0 FROM relato WHERE id_categoria = :id")
                .param("id", categoriaId)
                .query(Boolean.class).single();
    }

    public boolean existePorBairro(Long bairroId) {
        return jdbc.sql("SELECT COUNT(*) > 0 FROM relato WHERE id_bairro = :id")
                .param("id", bairroId)
                .query(Boolean.class).single();
    }

    // criadoEm e o dono não mudam em atualizações; a data é gerada pelo banco na criação
    public Risco salvar(Risco risco) {
        Long id = risco.id();
        if (id == null) {
            KeyHolder chave = new GeneratedKeyHolder();
            jdbc.sql("INSERT INTO relato (id_usuario, id_categoria, id_bairro, titulo, descricao, "
                            + "latitude, longitude, gravidade, status) "
                            + "VALUES (:usuario, :categoria, :bairro, :titulo, :descricao, "
                            + ":latitude, :longitude, :gravidade, :status)")
                    .param("usuario", risco.criadoPorId())
                    .param("categoria", risco.categoriaId())
                    .param("bairro", risco.bairroId())
                    .param("titulo", risco.titulo())
                    .param("descricao", risco.descricao())
                    .param("latitude", risco.latitude())
                    .param("longitude", risco.longitude())
                    .param("gravidade", risco.gravidade())
                    .param("status", risco.status().name())
                    .update(chave);
            id = chave.getKey().longValue();
        } else {
            jdbc.sql("UPDATE relato SET id_categoria = :categoria, id_bairro = :bairro, "
                            + "titulo = :titulo, descricao = :descricao, latitude = :latitude, "
                            + "longitude = :longitude, gravidade = :gravidade, status = :status "
                            + "WHERE id_relato = :id")
                    .param("categoria", risco.categoriaId())
                    .param("bairro", risco.bairroId())
                    .param("titulo", risco.titulo())
                    .param("descricao", risco.descricao())
                    .param("latitude", risco.latitude())
                    .param("longitude", risco.longitude())
                    .param("gravidade", risco.gravidade())
                    .param("status", risco.status().name())
                    .param("id", id)
                    .update();
        }
        return buscarPorId(id).orElseThrow();
    }

    // Os comentários e confirmações do risco somem junto (ON DELETE CASCADE)
    public void remover(Long id) {
        jdbc.sql("DELETE FROM relato WHERE id_relato = :id").param("id", id).update();
    }

    private Risco mapear(ResultSet rs, int linha) throws SQLException {
        return new Risco(
                rs.getLong("id_relato"),
                rs.getString("titulo"),
                rs.getString("descricao"),
                rs.getDouble("latitude"),
                rs.getDouble("longitude"),
                rs.getInt("gravidade"),
                StatusRisco.valueOf(rs.getString("status")),
                rs.getLong("id_categoria"),
                rs.getLong("id_bairro"),
                rs.getLong("id_usuario"),
                rs.getObject("data_criacao", LocalDateTime.class));
    }
}
