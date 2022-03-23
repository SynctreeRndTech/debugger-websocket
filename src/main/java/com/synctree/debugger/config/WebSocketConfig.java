package com.synctree.debugger.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebSocketHandler webSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketHandler, "/api/sock-js")
                //.setAllowedOrigins("*")
        		.setAllowedOriginPatterns("*")
                .withSockJS(); //sockjs 지원
        registry.addHandler(webSocketHandler, "/api/websocket")
        		.setAllowedOrigins("*"); // websocket 지원
    }

}