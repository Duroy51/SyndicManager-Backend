package com.enspy.syndicmanager.client.tokenHandler;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

@Component
@Order(1)
@Slf4j
public class TokenContextWebFilter implements Filter {

    private static final String TOKEN_CONTEXT_KEY = "auth-token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    // ThreadLocal pour stocker le token (équivalent du contexte Reactor)
    private static final ThreadLocal<String> TOKEN_HOLDER = new ThreadLocal<>();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {

        if (!(servletRequest instanceof HttpServletRequest httpRequest)) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        String requestId = java.util.UUID.randomUUID().toString().substring(0, 8);
        String requestPath = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        log.info("🔍 [{}] SERVLET FILTER START - {} {}", requestId, method, requestPath);

        try {
            // Log de tous les headers pour diagnostic (même logique que votre WebFilter)
            log.debug("📋 [{}] Headers reçus:", requestId);
            Collections.list(httpRequest.getHeaderNames()).forEach(name -> {
                String headerValue = httpRequest.getHeader(name);
                if (name.toLowerCase().contains("auth")) {
                    // Masquer le token pour la sécurité mais montrer qu'il existe
                    String maskedValue = headerValue != null && headerValue.length() > 30 ?
                            headerValue.substring(0, 30) + "..." : headerValue;
                    log.info("   🔐 [{}] {}: {}", requestId, name, maskedValue);
                } else {
                    log.debug("   📌 [{}] {}: {}", requestId, name, headerValue);
                }
            });

            // Vérification spécifique du header Authorization (même logique)
            String authHeader = httpRequest.getHeader(AUTHORIZATION_HEADER);
            if (authHeader != null) {
                log.info("✅ [{}] Header Authorization trouvé: {}...", requestId,
                        authHeader.length() > 20 ? authHeader.substring(0, 20) : authHeader);
            } else {
                log.warn("❌ [{}] Aucun header Authorization trouvé!", requestId);
                log.debug("🔍 [{}] Headers disponibles: {}", requestId,
                        Collections.list(httpRequest.getHeaderNames()));
            }

            // Extraction du token (même logique que votre WebFilter)
            Optional<String> tokenOpt = extractToken(httpRequest, requestId);

            if (tokenOpt.isPresent()) {
                String token = tokenOpt.get();
                log.info("🎯 [{}] Token extrait avec succès: {}...", requestId,
                        token.length() > 15 ? token.substring(0, 15) : token);
                log.info("🔗 [{}] Ajout du token au ThreadLocal", requestId);

                // Stockage dans ThreadLocal (équivalent du contexte Reactor)
                TOKEN_HOLDER.set(token);
                log.debug("📝 [{}] Écriture dans le ThreadLocal: token présent={}", requestId, token != null);
                log.debug("✅ [{}] ThreadLocal écrit avec succès", requestId);
            } else {
                log.warn("🚫 [{}] Aucun token valide trouvé, continuation sans contexte", requestId);
                TOKEN_HOLDER.remove(); // Nettoyer si pas de token
                log.debug("➡️ [{}] Requête continuée sans token", requestId);
            }

            // Continuation de la chaîne de filtres
            filterChain.doFilter(servletRequest, servletResponse);

        } catch (Exception error) {
            log.error("❌ [{}] Erreur dans le ServletFilter: {}", requestId, error.getMessage(), error);
            throw error;
        } finally {
            // Important: Nettoyer le ThreadLocal à la fin (équivalent du doFinally)
            TOKEN_HOLDER.remove();
            log.debug("🏁 [{}] SERVLET FILTER END - ThreadLocal nettoyé", requestId);
        }
    }

    // Même logique d'extraction que votre WebFilter
    private Optional<String> extractToken(HttpServletRequest request, String requestId) {
        log.debug("🔍 [{}] Début extraction token...", requestId);

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authHeader == null) {
            log.warn("⚠️ [{}] Header '{}' absent", requestId, AUTHORIZATION_HEADER);
            return Optional.empty();
        }

        log.debug("📋 [{}] Header Authorization: {}", requestId,
                authHeader.length() > 30 ? authHeader.substring(0, 30) + "..." : authHeader);

        if (!authHeader.startsWith(BEARER_PREFIX)) {
            log.warn("⚠️ [{}] Header Authorization ne commence pas par '{}' - Valeur: {}",
                    requestId, BEARER_PREFIX, authHeader.substring(0, Math.min(20, authHeader.length())));
            return Optional.empty();
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        if (token.trim().isEmpty()) {
            log.warn("⚠️ [{}] Token vide après extraction du préfixe Bearer", requestId);
            return Optional.empty();
        }

        log.info("✅ [{}] Token extrait: longueur={}, début={}...",
                requestId, token.length(),
                token.length() > 10 ? token.substring(0, 10) : token);

        return Optional.of(token);
    }

    /**
     * Méthodes statiques pour accéder au token depuis n'importe où
     * (équivalent de TokenContextUtils.getCurrentToken())
     */
    public static Optional<String> getCurrentToken() {
        String token = TOKEN_HOLDER.get();
        return Optional.ofNullable(token);
    }

    public static boolean hasToken() {
        return TOKEN_HOLDER.get() != null;
    }
}