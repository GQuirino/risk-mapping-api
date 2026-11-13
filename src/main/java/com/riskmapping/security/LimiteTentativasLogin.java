package com.riskmapping.security;

import com.riskmapping.exception.TentativasExcedidasException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Barra força bruta no login. Conta as FALHAS dentro de uma janela de 15 minutos
// e bloqueia (HTTP 429) quando passa do limite. Três contadores:
//   1. e-mail + IP : 5 falhas  -> trava quem está chutando a senha de uma conta
//                                  (sem deixar um atacante bloquear a vítima de outro IP)
//   2. só o IP     : 20 falhas -> trava quem testa muitos e-mails diferentes
//   3. só o e-mail : 50 falhas -> trava ataque distribuído (muitos IPs) contra uma conta
// Fica em memória, como o resto dos dados: reiniciar a API zera os contadores.
@Component
public class LimiteTentativasLogin {

    private static final Duration JANELA = Duration.ofMinutes(15);
    private static final int MAX_EMAIL_E_IP = 5;
    private static final int MAX_POR_IP = 20;
    private static final int MAX_POR_EMAIL = 50;
    private static final int MAX_CHAVES_GUARDADAS = 10_000; // evita encher a memória

    private record Contador(int falhas, Instant inicio) {
        boolean expirou() {
            return inicio.plus(JANELA).isBefore(Instant.now());
        }
    }

    private final Map<String, Contador> contadores = new ConcurrentHashMap<>();

    // Chame ANTES de verificar a senha
    public void exigirNaoBloqueado(String ip, String email) {
        String e = normalizar(email);
        if (falhas("ei:" + e + "|" + ip) >= MAX_EMAIL_E_IP
                || falhas("ip:" + ip) >= MAX_POR_IP
                || falhas("em:" + e) >= MAX_POR_EMAIL) {
            throw new TentativasExcedidasException(
                    "Muitas tentativas de login. Tente novamente em alguns minutos.");
        }
    }

    public void registrarFalha(String ip, String email) {
        String e = normalizar(email);
        somar("ei:" + e + "|" + ip);
        somar("ip:" + ip);
        somar("em:" + e);
        limparExpirados();
    }

    // Login certo zera só o contador "e-mail + IP". Os contadores por IP e por e-mail
    // continuam, senão dava para "resetar" o limite entrando na própria conta entre os chutes.
    public void registrarSucesso(String ip, String email) {
        contadores.remove("ei:" + normalizar(email) + "|" + ip);
    }

    private int falhas(String chave) {
        Contador c = contadores.get(chave);
        if (c == null) {
            return 0;
        }
        if (c.expirou()) {
            contadores.remove(chave, c);
            return 0;
        }
        return c.falhas();
    }

    private void somar(String chave) {
        contadores.merge(chave, new Contador(1, Instant.now()),
                (atual, novo) -> atual.expirou()
                        ? novo
                        : new Contador(atual.falhas() + 1, atual.inicio()));
    }

    private void limparExpirados() {
        if (contadores.size() > MAX_CHAVES_GUARDADAS) {
            contadores.values().removeIf(Contador::expirou);
        }
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
