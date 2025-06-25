package com.enspy.syndicmanager.client.services;

import com.enspy.syndicmanager.dto.response.ResponseDto; // Votre ResponseDto
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
@Slf4j
public class ApiService {

    private final WebClient webClient;

    /**
     * Envoie une requête HTTP et gère la réponse, permettant de spécifier le type de corps attendu.
     * Le corps de la réponse désérialisé sera placé dans le champ 'data' du ResponseDto.
     *
     * @param method                La méthode HTTP (GET, POST, etc.).
     * @param endpointUri           L'URI de l'endpoint (sans la base URL).
     * @param requestBody           Le corps de la requête (peut être null pour GET, DELETE).
     * @param expectedResponseBodyType Le ParameterizedTypeReference pour le type Java dans lequel le corps de réponse doit être désérialisé.
     *                              Utiliser `new ParameterizedTypeReference<Void>() {}` si aucun corps n'est attendu ou si vous voulez l'ignorer.
     *                              Le résultat sera mis dans ResponseDto.data.
     * @param <T_REQ_BODY>          Type du corps de la requête.
     * @param <T_EXPECTED_RES_BODY> Type Java attendu pour le corps de la réponse (sera mis dans ResponseDto.data).
     * @return Un Mono contenant le ResponseDto.
     */
    public <T_REQ_BODY, T_EXPECTED_RES_BODY> Mono<ResponseDto> sendRequest(
            HttpMethod method,
            String endpointUri,
            T_REQ_BODY requestBody,
            ParameterizedTypeReference<T_EXPECTED_RES_BODY> expectedResponseBodyType
    ) {
        log.info("🔄 [API_SERVICE] Début de la requête: {} {}, Type de réponse attendu pour désérialisation: {}",
                method, endpointUri, expectedResponseBodyType.getType().getTypeName());

        boolean hasRequestBody = (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH);

        if (hasRequestBody && requestBody == null) {
            log.error("❌ [API_SERVICE] RequestBody est null pour la méthode {} qui requiert un corps.", method);
            ResponseDto errorResponse = ResponseDto.builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .text("Le corps de la requête (requestBody) ne peut pas être null pour la méthode " + method)
                    .build();
            return Mono.just(errorResponse);
        }

        WebClient.RequestBodySpec requestSpec = webClient
                .method(method)
                .uri(endpointUri);

        WebClient.RequestHeadersSpec<?> finalRequestSpec = hasRequestBody
                ? requestSpec.bodyValue(requestBody)
                : requestSpec;

        if (hasRequestBody) {
            log.debug("📤 [API_SERVICE] Envoi avec corps: {}", requestBody);
        } else {
            log.debug("📤 [API_SERVICE] Envoi sans corps.");
        }

        return finalRequestSpec
                .exchangeToMono(response -> handleClientResponse(response, expectedResponseBodyType, endpointUri, method))
                .doOnSuccess(responseDto -> {
                    if (responseDto.getStatus() >= 200 && responseDto.getStatus() < 300) {
                        log.info("✅ [API_SERVICE] Requête {} {} réussie: status {}, type de data: {}",
                                method, endpointUri, responseDto.getStatus(),
                                responseDto.getData() != null ? responseDto.getData().getClass().getSimpleName() : "null");
                    } else {
                        log.warn("⚠️ [API_SERVICE] Requête {} {} terminée avec un statut d'erreur dans ResponseDto: status {}, message: {}",
                                method, endpointUri, responseDto.getStatus(), responseDto.getText());
                    }
                })
                .retryWhen(Retry.backoff(2, Duration.ofSeconds(1)) // Max 2 retries, 1s backoff
                        .filter(ex -> {
                            boolean shouldRetry = ex instanceof WebClientResponseException wcre && wcre.getStatusCode().is5xxServerError() ||
                                    (ex.getMessage() != null &&
                                            (ex.getMessage().toLowerCase().contains("connection reset") ||
                                                    ex.getMessage().toLowerCase().contains("timeout") ||
                                                    ex.getMessage().toLowerCase().contains("service unavailable")));
                            if (shouldRetry) {
                                log.warn("⚠️ [API_SERVICE] Retry pour {}: {} {}. Erreur: {}", ex.getClass().getSimpleName(), method, endpointUri, ex.getMessage());
                            } else {
                                log.debug("🚫 [API_SERVICE] Pas de retry pour {}: {} {}. Erreur: {}", ex.getClass().getSimpleName(), method, endpointUri, ex.getMessage());
                            }
                            return shouldRetry;
                        })
                        .doBeforeRetry(retrySignal -> log.info("🔁 [API_SERVICE] Tentative de retry #{} pour {} {} après échec: {}",
                                retrySignal.totalRetries() + 1, method, endpointUri, retrySignal.failure().getMessage()))
                )
                .onErrorResume(ex -> {
                    log.error("❌ [API_SERVICE] Erreur finale non récupérable pour {} {}: {}", method, endpointUri, ex.getMessage(), ex);
                    ResponseDto.ResponseDtoBuilder errorDtoBuilder = ResponseDto.builder();
                    if (ex instanceof WebClientResponseException wcre) {
                        log.error("🌐 [API_SERVICE] WebClientResponseException: status={}, body={}",
                                wcre.getStatusCode(), wcre.getResponseBodyAsString());
                        errorDtoBuilder
                                .status(wcre.getStatusCode().value())
                                .text("Erreur HTTP " + wcre.getStatusCode().value() + " (" + wcre.getStatusText() + "): " + wcre.getResponseBodyAsString());
                    } else {
                        log.error("💥 [API_SERVICE] Erreur technique/inattendue: {}", ex.getMessage());
                        errorDtoBuilder
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .text("Erreur technique inattendue: " + ex.getMessage());
                    }
                    return Mono.just(errorDtoBuilder.build());
                });
    }

    private <T_EXPECTED_RES_BODY> Mono<ResponseDto> handleClientResponse(
            ClientResponse clientResponse,
            ParameterizedTypeReference<T_EXPECTED_RES_BODY> expectedResponseBodyType,
            String endpointUri,
            HttpMethod method
    ) {
        HttpStatusCode statusCode = clientResponse.statusCode();
        log.info("📥 [API_SERVICE] Réponse reçue de {} {}: Status {}", method, endpointUri, statusCode);

        if (statusCode.isError()) {
            log.warn("👎 [API_SERVICE] Réponse d'erreur HTTP: {} pour {} {}", statusCode, method, endpointUri);

            return clientResponse.createException().flatMap(Mono::error);
        }

        Type actualType = expectedResponseBodyType.getType();

        if (actualType.equals(Void.class) || statusCode == HttpStatus.NO_CONTENT) {
            log.info("📬 [API_SERVICE] Réponse sans corps attendu/pertinent (type Void ou status {} {}) pour {} {}. Status: {}",
                    statusCode == HttpStatus.NO_CONTENT ? "204" : "autre",
                    statusCode, method, endpointUri, statusCode);


            return clientResponse.bodyToMono(Void.class)
                    .then(Mono.fromCallable(() -> {
                        log.debug("[API_SERVICE] Corps consommé pour Void/204, construction de ResponseDto.");
                        return buildSuccessResponseDto(statusCode, null, getSuccessMessage(statusCode.value()));
                    }))
                    .defaultIfEmpty(buildSuccessResponseDto(statusCode, null, getSuccessMessage(statusCode.value())));
        }

        log.debug("📖 [API_SERVICE] Tentative de lecture du corps pour {} {}, type de désérialisation attendu: {}", method, endpointUri, actualType.getTypeName());
        return clientResponse.bodyToMono(expectedResponseBodyType)
                .map(bodyContent -> {
                    log.info("👍 [API_SERVICE] Corps de réponse désérialisé avec succès pour {} {}.", method, endpointUri);
                    return buildSuccessResponseDto(statusCode, bodyContent, getSuccessMessage(statusCode.value()));
                })
                .defaultIfEmpty(buildSuccessResponseDto(statusCode, getDefaultEmptyValue(expectedResponseBodyType), "Opération réussie, réponse vide reçue."))
                .onErrorResume(e -> {
                    log.error("‼️ [API_SERVICE] Erreur lors de la désérialisation du corps de la réponse pour {} {}: {}", method, endpointUri, e.getMessage(), e);
                    ResponseDto errorDto = ResponseDto.builder()
                            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                            .text("Erreur de décodage du corps de la réponse: " + e.getMessage())
                            .build();
                    return Mono.just(errorDto);
                });
    }

    private ResponseDto buildSuccessResponseDto(HttpStatusCode statusCode, Object data, String message) {
        ResponseDto dto = ResponseDto.builder()
                .status(statusCode.value())
                .data(data)
                .text(message)
                .build();
        log.debug("✅ [API_SERVICE] ResponseDto construit: status={}, type de data={}, message='{}'",
                statusCode.value(), (data != null ? data.getClass().getSimpleName() : "null"), message);
        return dto;
    }

    /**
     * Fournit une valeur vide par défaut en fonction du type attendu pour la désérialisation.
     *
     */
    @SuppressWarnings("unchecked")
    private <T_EXPECTED_RES_BODY> T_EXPECTED_RES_BODY getDefaultEmptyValue(ParameterizedTypeReference<T_EXPECTED_RES_BODY> typeRef) {
        Type type = typeRef.getType();

        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (List.class.isAssignableFrom(clazz)) {
                return (T_EXPECTED_RES_BODY) Collections.emptyList();
            } else if (Map.class.isAssignableFrom(clazz)) {
                return (T_EXPECTED_RES_BODY) Collections.emptyMap();
            }
        } else if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class<?>) {
                Class<?> rawClass = (Class<?>) rawType;
                if (List.class.isAssignableFrom(rawClass)) {
                    return (T_EXPECTED_RES_BODY) Collections.emptyList();
                } else if (Map.class.isAssignableFrom(rawClass)) {
                    return (T_EXPECTED_RES_BODY) Collections.emptyMap();
                }
            }
        }

        log.debug("[API_SERVICE] getDefaultEmptyValue: Type '{}' n'est pas List ou Map, retourne null par défaut.", type.getTypeName());
        return null;
    }

    private String getSuccessMessage(int statusCode) {
        return switch (statusCode) {
            case 200 -> "Opération réussie";
            case 201 -> "Ressource créée avec succès";
            case 202 -> "Requête acceptée pour traitement";
            case 204 -> "Opération réussie, aucun contenu à retourner";
            default -> "Opération réussie avec statut " + statusCode;
        };
    }
}