package com.trivialware.hotelms.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {
    //Swagger Configuration
    @Bean
    public GroupedOpenApi apiGroup() {
        return GroupedOpenApi.builder().group("API").pathsToMatch("/**").build();
    }

    @Bean
    public OpenAPI apiInfo() {
        String securitySchemeName = "Authorization";
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .type(SecurityScheme.Type.HTTP)
                                                .bearerFormat("JWT")
                                                .scheme("bearer")
                                )
                )
                .info(
                        new Info().
                                title("HotelMS REST Api").
                                description("REST API for Hotel Management System").
                                version("1.0").
                                license(
                                        new License().
                                                name("GNU Affero General Public License v3.0 or later").
                                                identifier("AGPL-3.0-or-later").
                                                url("https://www.gnu.org/licenses/agpl-3.0.txt")
                                )
                );
    }
}
