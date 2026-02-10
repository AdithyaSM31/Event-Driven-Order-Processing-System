package com.eventdriven.gateway.config;

import com.eventdriven.gateway.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
public class GatewayConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        corsConfig.setMaxAge(3600L);
        corsConfig.addAllowedMethod("*");
        corsConfig.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfig);

        return new CorsWebFilter(source);
    }

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Order Service Routes
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://order-service"))
                
                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://payment-service"))
                
                // Inventory Service Routes
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://inventory-service"))
                
                // Notification Service Routes (if needed)
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f.filter(jwtAuthenticationFilter))
                        .uri("lb://notification-service"))
                
                // Health checks (no auth required)
                .route("health-checks", r -> r
                        .path("/actuator/**")
                        .uri("lb://order-service"))
                
                .build();
    }
}
