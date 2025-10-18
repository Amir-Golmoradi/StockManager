package dev.amirgol.stockmanager.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI configuration with JWT security scheme.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI stockManagerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Stock Manager API")
                        .description("E-commerce Stock Management System with Strict Authentication")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Amir Gol")
                                .email("contact@amirgol.dev"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Enter JWT token")));
    }
}