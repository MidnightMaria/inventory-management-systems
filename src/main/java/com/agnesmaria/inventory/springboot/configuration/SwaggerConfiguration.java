package com.agnesmaria.inventory.springboot.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.Getter;
import lombok.Setter;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

import static io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "swagger")
public class SwaggerConfiguration {

    private String appName;
    private String appDescription;
    private String appVersion;
    private String appLicense;
    private String appLicenseUrl;
    private String contactName;
    private String contactUrl;
    private String contactMail;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server")
                ))
                .info(getApiInformation())
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                );
    }

    private Info getApiInformation() {
        return new Info()
                .title(appName)
                .version(appVersion)
                .description(appDescription)
                .license(new License()
                        .name(appLicense)
                        .url(appLicenseUrl))
                .contact(new Contact()
                        .name(contactName)
                        .url(contactUrl)
                        .email(contactMail));
    }

    // Grouped APIs
    @Bean
    GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("Authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("Products")
                .pathsToMatch("/api/products/**")
                .build();
    }

    @Bean
    GroupedOpenApi warehouseApi() {
        return GroupedOpenApi.builder()
                .group("Warehouses")
                .pathsToMatch("/api/warehouses/**")
                .build();
    }

    @Bean
    GroupedOpenApi inventoryApi() {
        return GroupedOpenApi.builder()
                .group("Inventory")
                .pathsToMatch("/api/inventory/**")
                .build();
    }

    @Bean
    GroupedOpenApi purchaseOrderApi() {
        return GroupedOpenApi.builder()
                .group("Purchase Orders")
                .pathsToMatch("/api/purchase-orders/**")
                .build();
    }

    @Bean
    GroupedOpenApi salesApi() {
        return GroupedOpenApi.builder()
                .group("Sales")
                .pathsToMatch("/api/sales/**")
                .build();
    }

    @Bean
    GroupedOpenApi salesReportApi() {
        return GroupedOpenApi.builder()
                .group("Sales Reports")
                .pathsToMatch("/api/sales/report/**")
                .build();
    }

    @Bean
    GroupedOpenApi supplierApi() {
        return GroupedOpenApi.builder()
                .group("Suppliers")
                .pathsToMatch("/api/suppliers/**")
                .build();
    }

    @Bean
    GroupedOpenApi managementApi() {
        return GroupedOpenApi.builder()
                .group("Management")
                .pathsToMatch("/actuator/**")
                .build();
    }
}
