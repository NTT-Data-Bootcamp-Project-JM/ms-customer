package com.nttdata.bank.customer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Published OpenAPI contract of the customer service.
 */
@Configuration
public class OpenApiConfig {

    /**
     * @return contract metadata and the bearer scheme
     */
    @Bean
    public OpenAPI customerOpenApi() {
        return secured("Customers", "Personal, business, VIP, and PYME customers.");
    }

    private static OpenAPI secured(String title, String description) {
        return new OpenAPI()
                .info(new Info().title(title).version("1.0.0").description(description))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"))
                .components(new Components().addSecuritySchemes("bearer-jwt", bearer()));
    }

    private static SecurityScheme bearer() {
        return new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT");
    }
}
