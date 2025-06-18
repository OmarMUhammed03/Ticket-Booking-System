package org.example.apigateway.filters;

import jakarta.validation.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthGlobalFilter implements GlobalFilter {

    private final WebClient authWebClient;

    private final List<String> excludedPaths = List.of("/api/auth");

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthGlobalFilter.class);

    public AuthGlobalFilter(final WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    @Override
    public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        boolean isExcluded = excludedPaths.stream().anyMatch(path::startsWith);
        if (isExcluded) {
            LOGGER.debug("Path {} is excluded from authentication filter", path);
            return chain.filter(exchange);
        }

        LOGGER.debug("Applying authentication filter to path: {}", path);

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn(
                    "Authorization header is missing or invalid for path: {} with authHeader: {}", path, authHeader);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        LOGGER.debug("Authorization header found. Calling authentication service.");

        return authWebClient
                .get()
                .uri("/api/auth/me")
                .header(HttpHeaders.AUTHORIZATION, authHeader)
                .retrieve()
                .onStatus(status -> status != HttpStatus.OK,
                        resp -> {
                            LOGGER.error("Authentication service returned non-OK status: {}. Headers: {}",
                                    resp.statusCode(), resp.headers().asHttpHeaders());
                            return resp.bodyToMono(String.class)
                                    .doOnNext(body -> LOGGER.error("Authentication service failed body: {}", body))
                                    .then(Mono.error(new
                                            ValidationException("Authentication service rejected request")));
                        })
                .toEntity(Void.class)
                .flatMap(authResponseEntity -> {
                    LOGGER.debug("Authentication service returned OK. Extracting headers.");
                    HttpHeaders authHeaders = authResponseEntity.getHeaders();
                    String userRoles = authHeaders.getFirst("X-User-Roles");
                    String userEmail = authHeaders.getFirst("X-User-Email");
                    String userId = authHeaders.getFirst("X-User-Id");

                    LOGGER.debug("Extracted headers - X-User-Roles: {}, X-User-Email: {}, X-User-Id: {}", userRoles,
                            userEmail, userId);

                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .headers(headers -> {
                                if (userRoles != null) {
                                    headers.add("X-User-Roles", userRoles);
                                }
                                if (userEmail != null) {
                                    headers.add("X-User-Email", userEmail);
                                }
                                if (userId != null) {
                                    headers.add("X-User-Id", userId);
                                }
                            })
                            .build();

                    ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

                    LOGGER.debug("Headers added to outgoing request. Continuing filter chain.");
                    return chain.filter(modifiedExchange);
                })
                .onErrorResume(err -> {
                    LOGGER.error("Authentication failed due to an error: {}", err.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                });
    }
}
