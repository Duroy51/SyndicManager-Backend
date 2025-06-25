package com.enspy.syndicmanager.client.tokenHandler;

import com.enspy.syndicmanager.config.UnauthorizedException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.util.List;
import java.util.Optional;



@Component
@Slf4j
public class TokenContextUtils {

    private static final String TOKEN_CONTEXT_KEY = "auth-token";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * MODIFIÉ: Récupère maintenant le token depuis le ServletFilter ThreadLocal
     * au lieu du contexte Reactor WebFilter
     */
    public static Mono<String> getCurrentToken() {
        return Mono.fromCallable(() -> {
                    log.debug("🔍 Récupération token depuis ServletFilter ThreadLocal...");

                    // Récupération depuis le ThreadLocal du ServletFilter
                    return TokenContextWebFilter.getCurrentToken()
                            .orElseThrow(() -> {
                                log.warn("❌ Aucun token trouvé dans ThreadLocal");
                                return new UnauthorizedException("No authentication token found");
                            });
                })
                .doOnNext(token -> {
                    String tokenPreview = token.length() > 10 ? token.substring(0, 10) + "..." : token;
                    log.debug("✅ Token récupéré depuis ThreadLocal: {}", tokenPreview);
                })
                .doOnError(error -> {
                    if (!(error instanceof UnauthorizedException)) {
                        log.error("💥 Erreur lors de la récupération du token: {}", error.getMessage());
                    }
                });
    }


    /**
     * Décode le payload du JWT token
     */
    public static Mono<JsonNode> decodeTokenPayload() {
        return getCurrentToken()
                .map(token -> {
                    try {
                        // Séparer les parties du JWT (header.payload.signature)
                        String[] parts = token.split("\\.");
                        if (parts.length != 3) {
                            throw new IllegalArgumentException("Token JWT invalide");
                        }

                        // Décoder le payload (partie 2)
                        byte[] decodedBytes = Base64.getUrlDecoder().decode(parts[1]);
                        String decodedPayload = new String(decodedBytes);

                        // Parser en JSON
                        return objectMapper.readTree(decodedPayload);
                    } catch (Exception e) {
                        log.error("❌ Erreur lors du décodage du token: {}", e.getMessage());
                        throw new RuntimeException("Erreur lors du décodage du token", e);
                    }
                });
    }

    /**
     * Récupère l'ID de l'utilisateur depuis le token
     */
    public static Mono<String> getCurrentUserId() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("id")) {
                        throw new UnauthorizedException("ID utilisateur non trouvé dans le token");
                    }
                    return userNode.get("id").asText();
                })
                .doOnNext(userId -> log.debug("🔍 User ID: {}", userId));
    }

    /**
     * Récupère le prénom de l'utilisateur depuis le token
     */
    public static Mono<String> getCurrentUserFirstName() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("firstName")) {
                        return "";
                    }
                    return userNode.get("firstName").asText();
                })
                .doOnNext(firstName -> log.debug("🔍 User First Name: {}", firstName));
    }

    /**
     * Récupère le nom de famille de l'utilisateur depuis le token
     */
    public static Mono<String> getCurrentUserLastName() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("lastName")) {
                        return "";
                    }
                    return userNode.get("lastName").asText();
                })
                .doOnNext(lastName -> log.debug("🔍 User Last Name: {}", lastName));
    }

    /**
     * Récupère le nom complet de l'utilisateur depuis le token
     */
    public static Mono<String> getCurrentUserFullName() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null) {
                        return "";
                    }

                    String firstName = userNode.has("firstName") ? userNode.get("firstName").asText() : "";
                    String lastName = userNode.has("lastName") ? userNode.get("lastName").asText() : "";

                    return (firstName + " " + lastName).trim();
                })
                .doOnNext(fullName -> log.debug("🔍 User Full Name: {}", fullName));
    }

    /**
     * Récupère le nom d'utilisateur depuis le token
     */
    public static Mono<String> getCurrentUsername() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("username")) {
                        throw new UnauthorizedException("Username non trouvé dans le token");
                    }
                    return userNode.get("username").asText();
                })
                .doOnNext(username -> log.debug("🔍 Username: {}", username));
    }

    /**
     * Récupère l'email de l'utilisateur depuis le token
     */
    public static Mono<String> getCurrentUserEmail() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("email")) {
                        throw new UnauthorizedException("Email non trouvé dans le token");
                    }
                    return userNode.get("email").asText();
                })
                .doOnNext(email -> log.debug("🔍 User Email: {}", email));
    }

    /**
     * Récupère le numéro de téléphone de l'utilisateur depuis le token
     */
    public static Mono<Optional<String>> getCurrentUserPhoneNumber() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("phoneNumber")) {
                        return Optional.<String>empty();
                    }
                    String phoneNumber = userNode.get("phoneNumber").asText();
                    return phoneNumber != null && !phoneNumber.isEmpty() ?
                            Optional.of(phoneNumber) : Optional.<String>empty();
                })
                .doOnNext(phone -> log.debug("🔍 User Phone: {}", phone.orElse("N/A")));
    }

    /**
     * Vérifie si l'email de l'utilisateur est vérifié
     */
    public static Mono<Boolean> isCurrentUserEmailVerified() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("emailVerified")) {
                        return false;
                    }
                    return userNode.get("emailVerified").asBoolean();
                })
                .doOnNext(verified -> log.debug("🔍 Email Verified: {}", verified));
    }

    /**
     * Vérifie si le numéro de téléphone de l'utilisateur est vérifié
     */
    public static Mono<Boolean> isCurrentUserPhoneVerified() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null || !userNode.has("phoneNumberVerified")) {
                        return false;
                    }
                    return userNode.get("phoneNumberVerified").asBoolean();
                })
                .doOnNext(verified -> log.debug("🔍 Phone Verified: {}", verified));
    }

    /**
     * Récupère les autorités/rôles de l'utilisateur depuis le token
     */
    public static Mono<List<String>> getCurrentUserAuthorities() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode authoritiesNode = payload.get("authorities");
                    if (authoritiesNode == null || !authoritiesNode.isArray()) {
                        return List.<String>of();
                    }

                    return objectMapper.convertValue(authoritiesNode,
                            objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                })
                .doOnNext(authorities -> log.debug("🔍 User Authorities: {}", authorities));
    }

    /**
     * Vérifie si l'utilisateur a un rôle spécifique
     */
    public static Mono<Boolean> hasAuthority(String authority) {
        return getCurrentUserAuthorities()
                .map(authorities -> authorities.contains(authority))
                .doOnNext(hasAuth -> log.debug("🔍 Has authority '{}': {}", authority, hasAuth));
    }

    /**
     * Récupère le subject (sub) du token
     */
    public static Mono<String> getCurrentUserSubject() {
        return decodeTokenPayload()
                .map(payload -> {
                    if (!payload.has("sub")) {
                        throw new UnauthorizedException("Subject non trouvé dans le token");
                    }
                    return payload.get("sub").asText();
                })
                .doOnNext(sub -> log.debug("🔍 Subject: {}", sub));
    }

    /**
     * Récupère l'issuer (iss) du token
     */
    public static Mono<String> getTokenIssuer() {
        return decodeTokenPayload()
                .map(payload -> {
                    if (!payload.has("iss")) {
                        return "";
                    }
                    return payload.get("iss").asText();
                })
                .doOnNext(iss -> log.debug("🔍 Issuer: {}", iss));
    }

    /**
     * Vérifie si le token est encore valide (non expiré)
     */
    public static Mono<Boolean> isTokenValid() {
        return decodeTokenPayload()
                .map(payload -> {
                    if (!payload.has("exp")) {
                        return false;
                    }
                    long exp = payload.get("exp").asLong();
                    long currentTime = System.currentTimeMillis() / 1000; // Timestamp en secondes
                    return exp > currentTime;
                })
                .doOnNext(valid -> log.debug("🔍 Token Valid: {}", valid));
    }

    /**
     * Récupère la date d'expiration du token
     */
    public static Mono<Long> getTokenExpirationTime() {
        return decodeTokenPayload()
                .map(payload -> {
                    if (!payload.has("exp")) {
                        return 0L;
                    }
                    return payload.get("exp").asLong();
                })
                .doOnNext(exp -> log.debug("🔍 Token Expiration: {}", exp));
    }

    /**
     * Récupère toutes les informations utilisateur sous forme d'objet
     */
    public static Mono<JsonNode> getCurrentUserInfo() {
        return decodeTokenPayload()
                .map(payload -> {
                    JsonNode userNode = payload.get("user");
                    if (userNode == null) {
                        throw new UnauthorizedException("Informations utilisateur non trouvées dans le token");
                    }
                    return userNode;
                })
                .doOnNext(userInfo -> log.debug("🔍 User Info retrieved"));
    }



}
