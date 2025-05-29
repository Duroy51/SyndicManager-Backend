package com.enspy.syndicmanager.services;


import com.enspy.syndicmanager.config.WebClientConfig;
import com.enspy.syndicmanager.dto.apiResponse.loginResponse;
import com.enspy.syndicmanager.dto.request.LoginDto;
import com.enspy.syndicmanager.dto.request.RegisterDto;
import com.enspy.syndicmanager.dto.response.ResponseDto;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class AuthService {
    private final WebClient webClient;
    private String authToken;

    public AuthService(WebClient webClient) {
        this.webClient = webClient;
    }
    public Mono<loginResponse> login (LoginDto loginDto) {
        return webClient.post()
                .uri("/auth-service/auth/login")
                .bodyValue(loginDto)
                .retrieve()
                .bodyToMono(loginResponse.class)
                .doOnNext(resp -> this.authToken = resp.getToken());
    }

    public Mono<ResponseDto> register(RegisterDto registerDto) {
        return webClient.post()
                .uri("/auth-service/auth/register")
                .bodyValue(registerDto)
                .exchangeToMono(this::handleResponse)                   // accès à ClientResponse :contentReference[oaicite:3]{index=3}
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))                     // 3 essais avec backoff de 1 s :contentReference[oaicite:4]{index=4}
                        .filter(throwable -> throwable instanceof WebClientResponseException)
                )
                .onErrorResume(ex -> {                                                 // après 3 échecs, on extrait le statut
                    if (ex instanceof WebClientResponseException wcre) {
                        ResponseDto errorDto = new ResponseDto();
                        errorDto.setStatus(wcre.getStatusCode().value());
                        errorDto.setText("Échec après 3 tentatives");
                        return Mono.just(errorDto);
                    }
                    return Mono.error(ex);
                });
    }

    private Mono<ResponseDto> handleResponse(ClientResponse response) {
        if (response.statusCode().is2xxSuccessful()) {
            return response.bodyToMono(ResponseDto.class);
        } else {
            // transforme la réponse HTTP en erreur pour déclencher retryWhen
            return response.createException()
                    .flatMap(Mono::error);                                  // lever exception sur statut non-2xx :contentReference[oaicite:5]{index=5}
        }
    }

}
