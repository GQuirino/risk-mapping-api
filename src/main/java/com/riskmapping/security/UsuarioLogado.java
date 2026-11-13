package com.riskmapping.security;

import com.riskmapping.exception.AcessoNegadoException;
import com.riskmapping.model.Perfil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// Os services usam esta classe para saber quem está fazendo a requisição.
// Só deve ser chamada em rotas que exigem login.
@Component
public class UsuarioLogado {

    public Long id() {
        return atual().id();
    }

    public boolean ehAdmin() {
        return atual().perfil() == Perfil.ADMIN;
    }

    // Regra "só o dono ou um ADMIN": usada em editar/apagar riscos e mitigações
    public void exigirDonoOuAdmin(Long donoId) {
        boolean ehDono = atual().id().equals(donoId);
        if (!ehDono && !ehAdmin()) {
            throw new AcessoNegadoException("Só o dono ou um administrador pode fazer isso");
        }
    }

    private UsuarioAutenticado atual() {
        return (UsuarioAutenticado) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
    }
}
