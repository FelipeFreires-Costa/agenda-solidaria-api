package br.edu.unicatolica.agendasolidaria.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI agendaSolidariaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Agenda Solidária API")
                .version("1.0.0")
                .description("MVP de API RESTful para associações comunitárias organizarem os atendimentos "
                        + "gratuitos prestados por profissionais voluntários (psicologia, nutrição, fisioterapia...). "
                        + "A tela da associação fica em http://localhost:8080/. "
                        + "Projeto da ACE 8 - Análise e Projeto de Sistemas II - UniCatólica.")
                .contact(new Contact().name(
                        "Felipe Freires da Costa, Gabriel Samilo Pinto de Oliveira, "
                                + "Eric Matheus da Silva Temoteo, Gustavo Barros Sampaio "
                                + "e Dherick de Sousa Bomfim")));
    }
}
