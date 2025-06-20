package com.enspy.syndicmanager.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import java.util.UUID;


@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl("http://example.com") // remplace par ton URL de base
                .filter(logRequest())
                .filter((request, next) -> {
                    // Transfère les attributs vers le contexte
                    return next.exchange(request)
                            .contextWrite(ctx -> {
                                Context updated = ctx;
                                if (request.attribute("request-id").isPresent()) {
                                    updated = updated.put("request-id", request.attribute("request-id").get());
                                }
                                if (request.attribute("start-time").isPresent()) {
                                    updated = updated.put("start-time", request.attribute("start-time").get());
                                }
                                return updated;
                            });
                })
                .filter(logResponse())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            String requestId = UUID.randomUUID().toString();
            long startTime = System.currentTimeMillis();

            // Log des infos
            log.info("""
                    ========== [REQUEST LOG - ID: {}] ==========
                    Method: {}
                    URL: {}
                    Headers: {}
                    =============================================
                    """,
                    requestId,
                    request.method(),
                    request.url(),
                    request.headers());

            // Ajoute les attributs à la requête
            ClientRequest mutatedRequest = ClientRequest.from(request)
                    .attribute("request-id", requestId)
                    .attribute("start-time", startTime)
                    .build();

            return Mono.just(mutatedRequest);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            // Copie le contenu de la réponse
            return response.bodyToMono(String.class)
                    .defaultIfEmpty("")
                    .flatMap(body -> {
                        return Mono.deferContextual(context -> {
                            String requestId = context.getOrDefault("request-id", "unknown");
                            long startTime = context.getOrDefault("start-time", System.currentTimeMillis() - 1000);
                            long duration = System.currentTimeMillis() - startTime;

                            log.info("""
                                    ========== [RESPONSE LOG - ID: {}] ==========
                                    Status: {}
                                    Headers: {}
                                    Body: {}
                                    Duration: {} ms
                                    ===============================================
                                    """,
                                    requestId,
                                    response.statusCode(),
                                    response.headers().asHttpHeaders(),
                                    body,
                                    duration);

                            // Reconstituer le ClientResponse avec le body consommé
                            return Mono.just(ClientResponse.from(response).body(body).build());
                        });
                    });
        });
    }

    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);
}
