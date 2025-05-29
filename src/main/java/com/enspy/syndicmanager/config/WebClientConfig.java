package com.enspy.syndicmanager.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration

public class WebClientConfig {

    private final ConcurrentMap<String, RequestMetadata> requestMetadataMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);


    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl("https://gateway.yowyob.com")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .filter(logRequest())
                .build();
    }


    private static class RequestMetadata {
        final long startTime;
        final String requestId;

        RequestMetadata(long startTime, String requestId) {
            this.startTime = startTime;
            this.requestId = requestId;
        }
    }

    private ExchangeFilterFunction logRequest() {
        return (request, next) -> {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            long startTime = System.currentTimeMillis();

            // Stocker les métadonnées
            requestMetadataMap.put(request.logPrefix(), new RequestMetadata(startTime, requestId));

            // Création d'un séparateur visuel pour améliorer la lisibilité
            String separator = "\n" + "=".repeat(80) + "\n";
            StringBuilder logMessage = new StringBuilder();

            // En-tête du message de log avec timestamp
            logMessage.append(separator)
                    .append(String.format("🔷 HTTP REQUEST [ID: %s] - %s", requestId, LocalDateTime.now()))
                    .append(separator);

            // Détails de base de la requête (utilisation des méthodes disponibles)
            logMessage.append(String.format("➡️ %s %s",
                            request.method().name(),
                            request.url().toString()))
                    .append("\n\n");

            // En-têtes de la requête
            logMessage.append("📋 HEADERS:\n");
            request.headers().forEach((name, values) -> {
                // Masquage des informations sensibles
                if (name.equalsIgnoreCase("Authorization")) {
                    logMessage.append(String.format("   %s: %s\n", name, "Bearer ********"));
                } else if (name.equalsIgnoreCase("Cookie") || name.toLowerCase().contains("token")) {
                    logMessage.append(String.format("   %s: %s\n", name, "********"));
                } else {
                    logMessage.append(String.format("   %s: %s\n", name, String.join(", ", values)));
                }
            });

            // Cookies - adapter selon la disponibilité de l'API
            try {
                logMessage.append("\n🍪 COOKIES:\n");
                if (request.cookies() != null && !request.cookies().isEmpty()) {
                    request.cookies().forEach((name, values) -> {
                        logMessage.append(String.format("   %s: %s\n", name,
                                name.toLowerCase().contains("auth") ? "********" : values.toString()));
                    });
                } else {
                    logMessage.append("   [Aucun cookie]\n");
                }
            } catch (Exception e) {
                logMessage.append("   [Information sur les cookies non disponible]\n");
            }

            // Contexte d'exécution
            logMessage.append("\n🔍 CONTEXT:\n");
            logMessage.append(String.format("   Thread: %s\n", Thread.currentThread().getName()));
            logMessage.append(String.format("   Request ID: %s\n", requestId));

            // Pied de page
            logMessage.append(separator);

            log.info(logMessage.toString());

            return next.exchange(request);
        };
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            String logPrefix = response.request().toString();
            RequestMetadata metadata = requestMetadataMap.remove(logPrefix);

            // Si pas de métadonnées, créer des valeurs par défaut
            String requestId = metadata != null ? metadata.requestId : "unknown";
            long startTime = metadata != null ? metadata.startTime : System.currentTimeMillis();
            long duration = System.currentTimeMillis() - startTime;

            // Création d'un séparateur visuel
            String separator = "\n" + "+".repeat(80) + "\n";
            StringBuilder logMessage = new StringBuilder();

            // En-tête du message de log
            logMessage.append(separator)
                    .append(String.format("🔶 HTTP RESPONSE [ID: %s] - %s", requestId, LocalDateTime.now()))
                    .append(separator);

            // Détails de la requête originale (si disponibles)
            try {
                logMessage.append(String.format("📌 Original Request: %s %s\n\n",
                        response.request().getMethod(),
                        response.request().getURI()));
            } catch (Exception e) {
                logMessage.append("📌 Original Request: [Information non disponible]\n\n");
            }

            // Status HTTP de la réponse avec émoji correspondant
            String statusEmoji = response.statusCode().is2xxSuccessful() ? "✅" :
                    response.statusCode().is3xxRedirection() ? "↪️" :
                            response.statusCode().is4xxClientError() ? "⚠️" : "❌";

            // Récupération sécurisée de la reason phrase
            String reasonPhrase = "";
            try {
                if (response.statusCode() instanceof HttpStatus) {
                    reasonPhrase = ((HttpStatus) response.statusCode()).getReasonPhrase();
                }
            } catch (Exception e) {
                // Ignorer l'erreur si la méthode n'est pas disponible
            }

            logMessage.append(String.format("%s STATUS: %d %s\n\n",
                    statusEmoji,
                    response.statusCode().value(),
                    reasonPhrase));

            // En-têtes de la réponse
            logMessage.append("📋 HEADERS:\n");
            try {
                response.headers().asHttpHeaders().forEach((name, values) -> {
                    // Masquage des informations sensibles
                    if (name.toLowerCase().contains("token") || name.toLowerCase().contains("auth")) {
                        logMessage.append(String.format("   %s: ********\n", name));
                    } else {
                        logMessage.append(String.format("   %s: %s\n", name, String.join(", ", values)));
                    }
                });
            } catch (Exception e) {
                logMessage.append("   [Information sur les en-têtes non disponible]\n");
            }

            // Cookies de la réponse
            logMessage.append("\n🍪 COOKIES:\n");
            try {
                if (response.cookies() != null && !response.cookies().isEmpty()) {
                    response.cookies().forEach((name, values) -> {
                        logMessage.append(String.format("   %s: %s\n", name,
                                name.toLowerCase().contains("auth") ? "********" : values.toString()));
                    });
                } else {
                    logMessage.append("   [Aucun cookie]\n");
                }
            } catch (Exception e) {
                logMessage.append("   [Information sur les cookies non disponible]\n");
            }

            // Informations de performance
            logMessage.append("\n⏱️ PERFORMANCE METRICS:\n");
            logMessage.append(String.format("   Duration: %d ms\n", duration));

            // Pied de page
            logMessage.append(separator);

            // Adaptation du niveau de log selon le code HTTP
            if (response.statusCode().is2xxSuccessful()) {
                log.info(logMessage.toString());
            } else if (response.statusCode().is4xxClientError()) {
                log.warn(logMessage.toString());
            } else if (response.statusCode().is5xxServerError()) {
                log.error(logMessage.toString());
            } else {
                log.info(logMessage.toString());
            }

            return Mono.just(response);
        });
    }
}
