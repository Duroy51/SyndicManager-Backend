package com.enspy.syndicmanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class corsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Permet toutes les routes
                        .allowedOrigins("http://localhost:3000") // Autorise l'origine du frontend
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Autorise les méthodes HTTP
                        .allowedHeaders("*") // Autorise tous les en-têtes
                        .allowCredentials(true); // Permet l'envoi des cookies ou des identifiants
            }
        };
    }
}
