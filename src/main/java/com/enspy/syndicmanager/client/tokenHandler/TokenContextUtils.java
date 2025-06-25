package com.enspy.syndicmanager.client.tokenHandler;

import com.enspy.syndicmanager.config.UnauthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class TokenContextUtils {

    private static final String TOKEN_CONTEXT_KEY = "auth-token";

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


}
