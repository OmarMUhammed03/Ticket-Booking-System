package org.example.apigateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Component
public class AuthGlobalFilter implements GlobalFilter {

    private final WebClient authWebClient;

    private final List<String> excludedPaths = List.of("/api/auth");

    private static final Logger log = LoggerFactory.getLogger(AuthGlobalFilter.class);

    public AuthGlobalFilter(WebClient authWebClient) {
        this.authWebClient = authWebClient;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        boolean isExcluded = excludedPaths.stream().anyMatch(path::startsWith);
        if (isExcluded) {
            log.debug("Path {} is excluded from authentication filter", path);
            return chain.filter(exchange);
        }

        log.debug("Applying authentication filter to path: {}", path);

        // 1. Retrieve the "accessToken" cookie from the incoming request
        // getCookies() returns a MultiValueMap<String, HttpCookie>
        Optional<String> accessToken = Optional.ofNullable(exchange.getRequest().getCookies().
                        getFirst("accessToken"))
                .map(HttpCookie::getValue);

        // If the cookie is not present, return Unauthorized immediately
        if (accessToken.isEmpty()) {
            log.warn("Access token cookie not found for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        log.debug("Access token cookie found. Calling authentication service.");

        // 2. Call the authentication service (/api/auth/me) and send the accessToken cookie
        return authWebClient
                .get()
                .uri("/api/auth/me")
                // Add the accessToken cookie to the auth service request
                .cookie("accessToken", accessToken.get())
                .retrieve()
                // Handle non-OK responses from the auth service (e.g., 401, 403)
                .onStatus(status -> status != HttpStatus.OK,
                        resp -> {
                            log.error("Authentication service returned non-OK status: {}. Headers: {}",
                                    resp.statusCode(), resp.headers().asHttpHeaders());
                            // Log body if available for debugging
                            return resp.bodyToMono(String.class)
                                    .doOnNext(body -> log.error("Authentication service failed body: {}", body))
                                    // Signal an error to trigger the onErrorResume below
                                    .then(Mono.error(new RuntimeException("Authentication service rejected request")));
                        })
                // Get the response entity to access headers before reading the body (which is Void here)
                .toEntity(Void.class)
                // 3. On successful authentication (status is OK)
                .flatMap(authResponseEntity -> {
                    log.debug("Authentication service returned OK. Extracting headers.");
                    // Retrieve headers from the authentication service response
                    HttpHeaders authHeaders = authResponseEntity.getHeaders();
                    String userRoles = authHeaders.getFirst("X-User-Roles");
                    String userEmail = authHeaders.getFirst("X-User-Email");
                    String userId = authHeaders.getFirst("X-User-Id");

                    log.debug("Extracted headers - X-User-Roles: {}, X-User-Email: {}, X-User-Id: {}", userRoles,
                            userEmail, userId);

                    // 4. Mutate the original request to add the extracted headers
                    ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                            .headers(headers -> {
                                // Add headers only if they were present in the auth service response
                                if (userRoles != null) {
                                    headers.add("X-User-Roles", userRoles);
                                }
                                if (userEmail != null) {
                                    headers.add("X-User-Email", userEmail);
                                }
                                if (userId != null) {
                                    headers.add("X-User-Id", userId);
                                }
                                // You could potentially add other headers here if needed
                            })
                            .build();

                    // Create a new exchange with the modified request
                    ServerWebExchange modifiedExchange = exchange.mutate().request(modifiedRequest).build();

                    log.debug("Headers added to outgoing request. Continuing filter chain.");
                    // 5. Continue the filter chain with the modified exchange
                    return chain.filter(modifiedExchange);
                })
                // 6. Handle any errors that occurred during the process (missing cookie handled above,
                //    but this catches auth service errors or WebClient issues)
                .onErrorResume(err -> {
                    log.error("Authentication failed due to an error: {}", err.getMessage());
                    // Set the response status to Unauthorized
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    // Complete the response, stopping the filter chain
                    return exchange.getResponse().setComplete();
                });
    }
}