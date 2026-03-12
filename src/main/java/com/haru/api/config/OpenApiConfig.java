package com.haru.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI haruOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Haru Japanese Word API")
                        .description("API documentation for HaruJapaneseWordApi")
                        .version("v1"));
    }
}
