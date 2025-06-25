package com.enspy.syndicmanager.client.utils;

import com.enspy.syndicmanager.client.tokenHandler.TokenContextUtils;
import com.enspy.syndicmanager.config.UnauthorizedException;
import com.enspy.syndicmanager.config.WebClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class WebClientUtils {

    private final ConcurrentMap<String, RequestMetadata> requestMetadataMap = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(WebClientConfig.class);


    /**
     * Filtre pour ajouter automatiquement le token d'authentification aux requêtes sortantes
     */
    public ExchangeFilterFunction addAuthToken() {
        return (request, next) -> {
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            String endpoint = request.url().getPath();

            log.debug("🔑 [{}] Début ajout token pour: {} {}", requestId, request.method(), endpoint);

            return Mono.deferContextual(contextView -> {
                        // Log du contexte disponible pour diagnostic
                        log.debug("🔍 [{}] Contexte disponible: {}", requestId,
                                contextView.stream().map(entry -> entry.getKey().toString()).collect(Collectors.toList()));

                        return TokenContextUtils.getCurrentToken()
                                .doOnSubscribe(subscription ->
                                        log.debug("🔄 [{}] Récupération token depuis contexte...", requestId))

                                .doOnNext(token -> {
                                    String tokenPreview = token != null && token.length() > 10 ?
                                            token.substring(0, 10) + "..." : "TOKEN_VIDE";
                                    log.info("✅ [{}] Token récupéré: {}", requestId, tokenPreview);
                                })

                                .flatMap(token -> {
                                    if (token == null || token.trim().isEmpty()) {
                                        log.warn("⚠️ [{}] Token vide ou null, requête sans authentification", requestId);
                                        return next.exchange(request);
                                    }

                                    // Construction de la requête modifiée avec le token
                                    ClientRequest modifiedRequest = ClientRequest.from(request)
                                            .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                            .build();

                                    // Vérification que le header a été ajouté
                                    String authHeader = modifiedRequest.headers().getFirst(HttpHeaders.AUTHORIZATION);
                                    if (authHeader != null) {
                                        String headerPreview = authHeader.length() > 20 ?
                                                authHeader.substring(0, 20) + "..." : authHeader;
                                        log.info("🎯 [{}] Header Authorization ajouté: {}", requestId, headerPreview);
                                    } else {
                                        log.error("❌ [{}] ERREUR: Header Authorization non ajouté!", requestId);
                                    }

                                    // Log de tous les headers pour diagnostic complet
                                    log.debug("📋 [{}] Headers finaux de la requête:", requestId);
                                    modifiedRequest.headers().forEach((name, values) -> {
                                        if (name.toLowerCase().contains("auth") || name.toLowerCase().contains("key")) {
                                            String maskedValue = values.toString().length() > 30 ?
                                                    values.toString().substring(0, 30) + "..." : values.toString();
                                            log.debug("   🔐 {}: {}", name, maskedValue);
                                        } else {
                                            log.debug("   📌 {}: {}", name, values);
                                        }
                                    });

                                    return next.exchange(modifiedRequest);
                                })

                                .doOnSuccess(response ->
                                        log.debug("✅ [{}] Requête avec token executée avec succès", requestId))

                                .doOnError(error ->
                                        log.error("❌ [{}] Erreur lors de l'ajout du token: {}", requestId, error.getMessage()))

                                .onErrorResume(error -> {
                                    // Gestion détaillée des erreurs
                                    if (error instanceof UnauthorizedException) {
                                        log.warn("🚫 [{}] Pas de token d'authentification disponible: {}",
                                                requestId, error.getMessage());
                                    } else {
                                        log.error("💥 [{}] Erreur technique lors de la récupération du token: {} - {}",
                                                requestId, error.getClass().getSimpleName(), error.getMessage());
                                    }

                                    // Continuer sans token (ou échouer selon votre stratégie)
                                    log.info("🔄 [{}] Continuation de la requête SANS authentification", requestId);
                                    return next.exchange(request);
                                });
                    })
                    .contextWrite(context -> {
                        // Préservation explicite du contexte pour la transmission
                        log.debug("🔗 [{}] Préservation du contexte Reactor", requestId);
                        return context;
                    });
        };
    }

    // Méthode utilitaire pour diagnostiquer le contexte (à ajouter dans WebClientUtils)
    public ExchangeFilterFunction debugContextFilter() {
        return (request, next) -> {
            return Mono.deferContextual(contextView -> {
                log.info("🔍 DEBUG CONTEXTE - Clés disponibles: {}",
                        contextView.stream()
                                .map(entry -> entry.getKey() + "=" +
                                        (entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null"))
                                .collect(Collectors.toList()));

                // Vérification spécifique des clés token
                contextView.stream()
                        .filter(entry -> entry.getKey().toString().toLowerCase().contains("token"))
                        .forEach(entry -> log.info("🎯 Token trouvé: clé='{}', valeur='{}'",
                                entry.getKey(),
                                entry.getValue().toString().length() > 20 ?
                                        entry.getValue().toString().substring(0, 20) + "..." :
                                        entry.getValue()));

                return next.exchange(request);
            });
        };
    }

    // Méthode pour forcer l'ajout du contexte si nécessaire (dernière solution)
    public ExchangeFilterFunction forceTokenContext(String hardcodedToken) {
        return (request, next) -> {
            log.warn("🚨 FORCE TOKEN - Utilisation d'un token hardcodé pour test");

            ClientRequest modifiedRequest = ClientRequest.from(request)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + hardcodedToken)
                    .build();

            return next.exchange(modifiedRequest);
        };
    }

    private record RequestMetadata(long startTime, String requestId) {
    }


    public  ExchangeFilterFunction logRequest() {
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

    public ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            // Récupération des métadonnées existantes
            // Impossible d'accéder à la requête d'origine ici.
// Utilise un identifiant contextuel ou loggue simplement la réponse sans rattacher à la requête.
            RequestMetadata meta = null; // ou gère différemment l'association
            String requestId = meta != null ? meta.requestId : "unknown";
            long duration = meta != null ? System.currentTimeMillis() - meta.startTime : -1;


            // On capture le status avant de lire le corps
            HttpStatus status = (HttpStatus) response.statusCode();
            String reason = status.getReasonPhrase();
            String statusEmoji = status.is2xxSuccessful() ? "✅" :
                    status.is3xxRedirection()    ? "↪️" :
                            status.is4xxClientError()    ? "⚠️" : "❌";

            // On clone le builder pour réinjecter le corps plus tard
            ClientResponse.Builder builder = ClientResponse.from(response);

            return response.bodyToMono(String.class)
                    .flatMap(body -> {
                        // Log du statut et du JSON de la réponse
                        log.info(
                                "\n🔶 HTTP RESPONSE [ID: {}] - {} {}\n" +
                                        "   Status: {} {} {}\n" +
                                        "   Duration: {} ms\n" +
                                        "🔶 BODY:\n{}\n",
                                requestId,
                                LocalDateTime.now(),
                                statusEmoji,
                                status.value(),
                                reason,
                                statusEmoji,
                                duration,
                                body
                        );

                        // Reconstruction du ClientResponse pour le downstream
                        return Mono.just(builder.body(body).build());
                    });
        });
    }



    /**
     * Filtre pour vérifier la santé du gateway avant les requêtes importantes
     */
    public ExchangeFilterFunction gatewayHealthCheckFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("🌐 Requête vers gateway: {} {}", clientRequest.method(), clientRequest.url());

            // Log des headers pour debug
            clientRequest.headers().forEach((name, values) -> {
                if (!name.toLowerCase().contains("key") && !name.toLowerCase().contains("token")) {
                    log.debug("📋 Header: {}={}", name, values);
                }
            });

            return Mono.just(clientRequest);
        });
    }

    /**
     * Filtre de retry spécifique aux erreurs de gateway
     */
    public ExchangeFilterFunction retryOnGatewayErrorFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {

            // Log du status pour monitoring
            log.debug("📡 Gateway response status: {}", clientResponse.statusCode());

            // Vérification spécifique aux erreurs gateway
            if (clientResponse.statusCode().is5xxServerError()) {
                log.warn("⚠️ Erreur serveur du gateway détectée: {}", clientResponse.statusCode());
            }

            if (clientResponse.statusCode().value() == 502 ||
                    clientResponse.statusCode().value() == 503 ||
                    clientResponse.statusCode().value() == 504) {
                log.error("🚨 Gateway indisponible: {}", clientResponse.statusCode());
            }

            return Mono.just(clientResponse);
        });
    }


    public ExchangeFilterFunction logBody() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            String requestId = UUID.randomUUID().toString().substring(0, 8);

            if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                log.warn("🔍 [{}] Réponse d'erreur détectée: {}", requestId, response.statusCode());

                // Cloner le body pour pouvoir le logger ET le retourner
                return response.bodyToMono(String.class)
                        .defaultIfEmpty(response.statusCode().toString())
                        .flatMap(body -> {
                            log.error("❌ [{}] Erreur HTTP {} - Body: {}",
                                    requestId, response.statusCode(), body);

                            // Recréer la response avec le body préservé
                            ClientResponse newResponse = ClientResponse.create(response.statusCode())
                                    .headers(headers -> headers.addAll(response.headers().asHttpHeaders()))
                                    .body(body) // Remettre le body
                                    .build();

                            return Mono.just(newResponse);
                        })
                        .doOnError(error ->
                                log.error("💥 [{}] Erreur lors du logging du body: {}", requestId, error.getMessage()));
            } else {
                // Pour les succès, pas besoin de logger le body (déjà fait dans logResponse)
                log.debug("✅ [{}] Succès HTTP {}", requestId, response.statusCode());
                return Mono.just(response);
            }
        });
    }
}
