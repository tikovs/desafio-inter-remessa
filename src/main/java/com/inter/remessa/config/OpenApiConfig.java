package com.inter.remessa.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Serviço de Remessa — Banco Inter")
                        .description("""
                                API REST de remessa financeira com conversão BRL → USD em tempo real \
                                via PTAX (Banco Central do Brasil).

                                Suporta Pessoa Física (limite R$10.000/dia) e \
                                Pessoa Jurídica (limite R$50.000/dia). \
                                A cotação é obtida da API pública do BCB e cacheada no Redis.\
                                """)
                        .version("1.0.0"));
    }
}
