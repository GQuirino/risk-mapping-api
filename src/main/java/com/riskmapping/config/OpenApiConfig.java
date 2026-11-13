package com.riskmapping.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Faz aparecer o botão "Authorize" no Swagger para colar o token JWT
@Configuration
@OpenAPIDefinition(
        info = @Info(title = "Risk Mapping API", version = "0.0.1", description = """
                API do mapa de riscos comunitário: usuários registram riscos (alagamento, buraco, \
                deslizamento...) em um bairro, comentam e confirmam ou negam os relatos dos outros.

                **Como autenticar:** faça `POST /auth/registrar` e `POST /auth/login`, copie o `token` \
                da resposta, clique em **Authorize** e cole o token (sem a palavra Bearer).

                **Quem pode o quê:** a leitura (categorias, bairros, riscos, comentários e confirmações) \
                é pública. Criar, comentar e votar exige login. Editar ou apagar um risco ou comentário \
                é só do dono ou de um ADMIN. Categorias, bairros e usuários de outras pessoas são só do ADMIN.

                Erros seguem o formato RFC 7807 (`detail` e, na validação, `erros` por campo)."""),
        security = @SecurityRequirement(name = "bearer"))
@SecurityScheme(name = "bearer", type = SecuritySchemeType.HTTP,
        scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
