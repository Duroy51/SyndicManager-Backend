package com.enspy.syndicmanager.client.services;

import com.enspy.syndicmanager.client.dto.response.LoginResponse;
import com.enspy.syndicmanager.dto.request.LoginDto;
import com.enspy.syndicmanager.dto.request.RegisterDto;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;

@Service
public class Authentication {

    private final WebClient webClient;
    private final WebClient tokenWebClient;
    private String authToken;

    private static final String TOKEN_ENDPOINT = "https://gateway.yowyob.com/auth-service/oauth/token";
    private static final String CLIENT_ID     = "test-client";
    private static final String CLIENT_SECRET = "secret";

    public Authentication(WebClient webClient, WebClient.Builder webClientBuilder) {
        this.webClient = Objects.requireNonNull(webClient, "WebClient cannot be null");

        // WebClient dédié à l'IdP pour récupérer le token client_credentials
        this.tokenWebClient = WebClient.builder()
                .baseUrl(TOKEN_ENDPOINT)
                .defaultHeaders(h -> h.setBasicAuth(CLIENT_ID, CLIENT_SECRET))
                .build();
    }

    /**
     * 1) Récupère le token OAuth2 (Client Credentials).
     * 2) Renvoie le Mono<String> du access_token.
     */
    private Mono<String> getClientToken() {
        return tokenWebClient.post()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("grant_type", "client_credentials")
                        .with("scope", "read write")
                )
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .doOnNext(tokenResponse -> {
                    // Debug : afficher la réponse complète
                    System.out.println("Réponse OAuth2 reçue: " + tokenResponse);
                    if (tokenResponse != null) {
                        System.out.println("Access token: " + tokenResponse.getAccessToken());
                        System.out.println("Token type: " + tokenResponse.getToken_type());
                        System.out.println("Expires in: " + tokenResponse.getExpires_in());
                    }
                })
                .flatMap(tokenResponse -> {
                    if (tokenResponse == null) {
                        return Mono.error(new RuntimeException("Réponse OAuth2 null"));
                    }
                    String accessToken = tokenResponse.getAccessToken();
                    if (accessToken == null || accessToken.trim().isEmpty()) {
                        return Mono.error(new RuntimeException("Access token null ou vide dans la réponse OAuth2"));
                    }
                    return Mono.just(accessToken);
                })
                .doOnError(ex -> System.err.println("Erreur lors de la récupération du token client: " + ex.getMessage()));
    }

    /**
     * 2) login : on commence par obtenir le token client, puis on appelle /login
     */
    public Mono<LoginResponse> login(LoginDto loginDto) {
        // Validation des paramètres d'entrée
        if (loginDto == null) {
            return Mono.error(new IllegalArgumentException("LoginDto ne peut pas être null"));
        }

        return getClientToken()
                .flatMap(clientToken -> {
                    if (clientToken == null || clientToken.trim().isEmpty()) {
                        return Mono.error(new RuntimeException("Token client invalide"));
                    }

                    return webClient.post()
                            .uri("/auth-service/api/login")
                            .headers(h -> h.setBearerAuth(clientToken))
                            .bodyValue(loginDto)
                            .retrieve()
                            .bodyToMono(LoginResponse.class);
                })
                .flatMap(response -> {
                    if (response == null) {
                        return Mono.error(new RuntimeException("Réponse de login null"));
                    }
                    return Mono.just(response);
                })
                .doOnNext(resp -> {
                    this.authToken = resp.getAccessToken().getToken();
                    System.out.println("Token utilisateur stocké avec succès");
                })
                .doOnError(ex -> System.err.println("Erreur lors du login: " + ex.getMessage()));
    }

    /**
     * 3) register : même principe
     */
    public Mono<ResponseDto> register(RegisterDto registerDto) {
        if (registerDto == null) {
            return Mono.error(new IllegalArgumentException("RegisterDto ne peut pas être null"));
        }

        return getClientToken()
                .flatMap(clientToken -> {
                    if (clientToken == null || clientToken.trim().isEmpty()) {
                        return Mono.error(new RuntimeException("Token client invalide"));
                    }

                    return webClient.post()
                            .uri("/auth-service/api/register")
                            .headers(h -> h.setBearerAuth(clientToken))
                            .bodyValue(registerDto)
                            .exchangeToMono(this::handleResponse);
                })
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                        .filter(ex -> ex instanceof WebClientResponseException)
                )
                .onErrorResume(ex -> {
                    System.err.println("Erreur lors du register: " + ex.getMessage());
                    if (ex instanceof WebClientResponseException wcre) {
                        ResponseDto errorDto = new ResponseDto();
                        errorDto.setStatus(wcre.getStatusCode().value());
                        errorDto.setText("Échec après 3 tentatives: " + ex.getMessage());
                        return Mono.just(errorDto);
                    }
                    ResponseDto errorDto = new ResponseDto();
                    errorDto.setStatus(500);
                    errorDto.setText("Erreur interne: " + ex.getMessage());
                    return Mono.just(errorDto);
                });
    }

    private Mono<ResponseDto> handleResponse(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            // 1) Lit le corps JSON comme Map<String,Object>
            return response.bodyToMono(new ParameterizedTypeReference<Map<String,Object>>() {})
                    .map(jsonMap -> {
                        // 2) Remplit votre DTO
                        ResponseDto dto = new ResponseDto();
                        dto.setStatus(response.statusCode().value());
                        dto.setData(jsonMap);      // <— le JSON désérialisé
                        return dto;
                    });
        } else {
            return response.createException()
                    .flatMap(Mono::error);
        }
    }


    // Getter pour vérifier si l'utilisateur est authentifié
    public boolean isAuthenticated() {
        return authToken != null && !authToken.trim().isEmpty();
    }


    public String getAuthToken() {
        return authToken;
    }

    // DTO interne pour désérialiser la réponse du flux Client Credentials
    private static class TokenResponse {
        private String access_token;
        private String token_type;
        private Long expires_in;
        private String scope;

        // Constructeur par défaut requis pour Jackson
        public TokenResponse() {}

        public String getAccessToken() {
            return access_token;
        }

        public void setAccess_token(String access_token) {
            this.access_token = access_token;
        }

        // getters/setters pour token_type, expires_in, scope si besoin
        public String getToken_type() {
            return token_type;
        }

        public void setToken_type(String token_type) {
            this.token_type = token_type;
        }

        public Long getExpires_in() {
            return expires_in;
        }

        public void setExpires_in(Long expires_in) {
            this.expires_in = expires_in;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        @Override
        public String toString() {
            return "TokenResponse{" +
                    "access_token='" + (access_token != null ? access_token.substring(0, Math.min(access_token.length(), 10)) + "..." : "null") + '\'' +
                    ", token_type='" + token_type + '\'' +
                    ", expires_in=" + expires_in +
                    ", scope='" + scope + '\'' +
                    '}';
        }
    }
}