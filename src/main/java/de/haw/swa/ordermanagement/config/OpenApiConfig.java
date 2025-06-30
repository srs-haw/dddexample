package de.haw.swa.ordermanagement.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Management System API")
                        .description("""
                                A comprehensive Domain Driven Design (DDD) example application for order management.
                                
                                This API demonstrates:
                                - Event-driven architecture with automatic shipping
                                - Complete order lifecycle management
                                - Product catalog management
                                - RESTful API design principles
                                
                                **Order Status Flow:**
                                PENDING → CONFIRMED → PAID → SHIPPED → DELIVERED
                                         ↓                      ↑
                                    CANCELLED              (automatic)
                                                               ↓
                                                         RETURNED (from DELIVERED)
                                
                                **Note:** Orders are automatically shipped when payment is processed via event-driven architecture.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("HAW Hamburg - Software Architecture Course")
                                .email("noreply@haw-hamburg.de")
                                .url("https://www.haw-hamburg.de"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Development Server"),
                        new Server()
                                .url("https://api.ordermanagement.example.com")
                                .description("Production Server (Example)")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Products")
                                .description("Product catalog management operations"),
                        new Tag()
                                .name("Orders")
                                .description("Order lifecycle management operations")
                ));
    }
}