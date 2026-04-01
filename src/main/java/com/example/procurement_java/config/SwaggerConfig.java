// SwaggerConfig.java
package com.example.procurement_java.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    private SecurityScheme createAPIKeyScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .description("Enter JWT token in format: Bearer {token}");
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("Local Server"),
                        new Server().url("https://api.procurement.com").description("Production Server")
                ))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
                .info(new Info()
                        .title("Procurement Java API")
                        .version("1.0.0")
                        .description("""
                                Procurement System REST API with JWT Security
                                
                                ## Authentication
                                This API uses JWT token-based authentication.
                                
                                ### Steps to authenticate:
                                1. Register a new user using `/auth/register`
                                2. Login using `/auth/login` to get a JWT token
                                3. Use the token in the `Authorization` header as: `Bearer {your-token}`
                                
                                ### Token Information:
                                - Token validity: 8 hours
                                - Token type: Bearer JWT
                                
                                ### Default Admin User:
                                - Username: admin
                                - Password: admin123
                                """)
                        .contact(new Contact()
                                .name("ARIGEN TECHNOLOGY PVT LTD.")
                                .email("info@arigentechnology.com")
                                .url("https://arigentechnology.com/"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description("Procurement Java Project Documentation")
                        .url("https://github.com/arigen-tech/procurement-java"));
    }
}