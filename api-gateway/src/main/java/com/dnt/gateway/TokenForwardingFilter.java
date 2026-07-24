package com.dnt.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class TokenForwardingFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        var cookies = exchange.getRequest().getCookies();
        if (cookies.containsKey("token")) {
            String token = cookies.getFirst("token").getValue();
            if (token != null && !token.isEmpty()) {
                exchange = exchange.mutate()
                    .request(r -> r.header("Authorization", "Bearer " + token))
                    .build();
            }
        }
        return chain.filter(exchange);
    }
}
