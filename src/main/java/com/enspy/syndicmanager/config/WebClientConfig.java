package com.enspy.syndicmanager.config;

import com.enspy.syndicmanager.client.utils.WebClientUtils;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@AllArgsConstructor
@Slf4j
public class WebClientConfig {

    WebClientUtils webClientUtils;


    @Bean
    @Primary
    public WebClient webClient(WebClient.Builder builder) {

        ConnectionProvider connectionProvider = ConnectionProvider.newConnection();

        // Configuration HTTP client avec timeouts mais SANS pool
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 10s connexion
                .option(ChannelOption.SO_KEEPALIVE, false)           // Pas de keep-alive
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))   // 30s read
                        .addHandlerLast(new WriteTimeoutHandler(20, TimeUnit.SECONDS))) // 20s write
                .responseTimeout(Duration.ofSeconds(45))             // 45s response
                .compress(false);                                    // Pas de compression

        return builder
                .baseUrl("https://gateway.yowyob.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))

                // Headers de base
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("application_id", "6e2b2960-3e7a-11f0-b955-8dc72e51fc52")
                .defaultHeader("Public-Key", "api_1748735619775_6e2b2960.8jMVrdiqr_j04GJLvMxI_SBdEA-R_qs9")

                // Configuration codecs
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(1 * 1024 * 1024); // 1MB
                })

                // Filtres essentiels
                .filter(webClientUtils.addAuthToken())
                .filter(webClientUtils.logRequest())
                .filter(webClientUtils.logResponse())
                .filter(webClientUtils.logBody())

                .build();
    }
}


