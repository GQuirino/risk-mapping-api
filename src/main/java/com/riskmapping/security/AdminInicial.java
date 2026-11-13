package com.riskmapping.security;

import com.riskmapping.model.Perfil;
import com.riskmapping.model.Usuario;
import com.riskmapping.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Garante o ADMIN ao subir a API: cria a conta se não existir e, se já existir,
// sincroniza a senha com a configurada. O perfil ADMIN vem do e-mail (veja UsuarioRepository).
// A senha é obrigatória: app.admin.senha (config/application.properties) ou ADMIN_SENHA.
@Component
public class AdminInicial implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicial.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String senhaConfigurada;

    public AdminInicial(UsuarioRepository usuarioRepository,
                        PasswordEncoder passwordEncoder,
                        @Value("${app.admin.email}") String email,
                        @Value("${app.admin.senha}") String senhaConfigurada) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email;
        this.senhaConfigurada = senhaConfigurada;
    }

    @Override
    public void run(String... args) {
        if (senhaConfigurada.isBlank()) {
            throw new IllegalStateException(
                    "Defina a senha do ADMIN em config/application.properties "
                            + "(app.admin.senha=...) ou na variável de ambiente ADMIN_SENHA");
        }

        Usuario existente = usuarioRepository.buscarPorEmail(email).orElse(null);
        Long id = existente != null ? existente.id() : null;
        String nome = existente != null ? existente.nome() : "Administrador";

        usuarioRepository.salvar(new Usuario(id, nome, email,
                passwordEncoder.encode(senhaConfigurada), Perfil.ADMIN));

        log.info("ADMIN {} -> e-mail: {}", existente != null ? "atualizado" : "criado", email);
    }
}
