package com.semihsahinoglu.gateway.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


import java.time.LocalDateTime;

@Component
public class ErrorUtil {

    private final ObjectMapper objectMapper;

    public ErrorUtil(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Mono<Void> buildError(ServerWebExchange exchange,
                                 HttpStatus status,
                                 String message) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ErrorResponse errorResponse = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                exchange.getRequest().getPath().value(),
                LocalDateTime.now()
        );

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(errorResponse);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }
}