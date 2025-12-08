package com.soukscan.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Configuration for WebClient beans for inter-service communication.
 */
@Configuration
public class WebClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    @Value("${services.vendor-service.url}")
    private String vendorServiceUrl;

    @Value("${services.product-service.url}")
    private String productServiceUrl;

    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            logger.info("WebClient Request: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            logger.info("WebClient Response: {}", response.statusCode());
            return Mono.just(response);
        });
    }

    private ExchangeFilterFunction propagateAuthHeader() {
        return (request, next) -> {
            String token = null;
            try {
                ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
                token = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
            } catch (Exception ignored) {}
            if (token != null && !token.isEmpty()) {
                ClientRequest newRequest = ClientRequest.from(request)
                        .header(HttpHeaders.AUTHORIZATION, token)
                        .build();
                return next.exchange(newRequest);
            }
            return next.exchange(request);
        };
    }

    @Bean(name = "vendorWebClient")
    public WebClient vendorWebClient() {
        return WebClient.builder()
                .baseUrl(vendorServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(propagateAuthHeader())
                .filter(logRequest())
                .filter(logResponse())
                .filter((request, next) -> next.exchange(request)
                        .timeout(Duration.ofSeconds(10))
                        .retry(2)
                        .onErrorResume(ex -> {
                            logger.error("Vendor-Service error: {}", ex.getMessage());
                            return Mono.error(new RuntimeException("Vendor-Service error: " + ex.getMessage()));
                        })
                )
                .build();
    }

    @Bean(name = "productWebClient")
    public WebClient productWebClient() {
        return WebClient.builder()
                .baseUrl(productServiceUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .filter(propagateAuthHeader())
                .filter(logRequest())
                .filter(logResponse())
                .filter((request, next) -> next.exchange(request)
                        .timeout(Duration.ofSeconds(10))
                        .retry(2)
                        .onErrorResume(ex -> {
                            logger.error("Product-Service error: {}", ex.getMessage());
                            return Mono.error(new RuntimeException("Product-Service error: " + ex.getMessage()));
                        })
                )
                .build();
    }
}