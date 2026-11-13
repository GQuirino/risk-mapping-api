package com.riskmapping.repository;

import com.riskmapping.model.Perfil;
import com.riskmapping.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UsuarioRepository {

    private static final String COLUNAS = "id_usuario, nome, email, senha_hash";

    private final JdbcClient jdbc;
    private final String emailAdmin;

    public UsuarioRepository(JdbcClient jdbc, @Value("${app.admin.email}") String emailAdmin) {
        this.jdbc = jdbc;
        this.emailAdmin = emailAdmin;
    }

    public List<Usuario> listar() {
        return jdbc.sql("SELECT " + COLUNAS + " FROM usuario ORDER BY id_usuario")
                .query(this::mapear).list();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM usuario WHERE id_usuario = :id")
                .param("id", id)
                .query(this::mapear).optional();
    }

    // A comparação ignora maiúsculas (collation utf8mb4_unicode_ci)
    public Optional<Usuario> buscarPorEmail(String email) {
        return jdbc.sql("SELECT " + COLUNAS + " FROM usuario WHERE email = :email")
                .param("email", email)
                .query(this::mapear).optional();
    }

    public boolean existePorEmail(String email) {
        return jdbc.sql("SELECT COUNT(*) > 0 FROM usuario WHERE email = :email")
                .param("email", email)
                .query(Boolean.class).single();
    }

    // O perfil do record é ignorado: não existe coluna para ele (veja Usuario).
    public Usuario salvar(Usuario usuario) {
        Long id = usuario.id();
        if (id == null) {
            KeyHolder chave = new GeneratedKeyHolder();
            jdbc.sql("INSERT INTO usuario (nome, email, senha_hash) VALUES (:nome, :email, :senha)")
                    .param("nome", usuario.nome())
                    .param("email", usuario.email())
                    .param("senha", usuario.senhaHash())
                    .update(chave);
            id = chave.getKey().longValue();
        } else {
            jdbc.sql("UPDATE usuario SET nome = :nome, email = :email, senha_hash = :senha "
                            + "WHERE id_usuario = :id")
                    .param("nome", usuario.nome())
                    .param("email", usuario.email())
                    .param("senha", usuario.senhaHash())
                    .param("id", id)
                    .update();
        }
        return buscarPorId(id).orElseThrow();
    }

    // Os relatos, comentários e confirmações do usuário somem junto (ON DELETE CASCADE)
    public void remover(Long id) {
        jdbc.sql("DELETE FROM usuario WHERE id_usuario = :id").param("id", id).update();
    }

    private Usuario mapear(ResultSet rs, int linha) throws SQLException {
        String email = rs.getString("email");
        Perfil perfil = email.equalsIgnoreCase(emailAdmin) ? Perfil.ADMIN : Perfil.USUARIO;
        return new Usuario(rs.getLong("id_usuario"), rs.getString("nome"), email,
                rs.getString("senha_hash"), perfil);
    }
}
