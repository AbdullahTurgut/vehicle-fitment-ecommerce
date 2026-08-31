package com.carmats.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_AUTH = "Bearer Authentication";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Vehicle Fitment E-Commerce REST API")
                        .version("1.0.0")
                        .description("Türkiye pazarına yönelik, araç marka/model/kasa/yıl uyumluluğu odaklı 3D oto paspas ve bagaj havuzu e-ticaret platformu REST API dokümantasyonu.")
                        .contact(new Contact()
                                .name("Carmats Development Team")
                                .email("info@carmats.local"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://carmats.local")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH))
                .components(new Components().addSecuritySchemes(BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token'ınızı 'Bearer <token>' formatında giriniz.")));
    }
}