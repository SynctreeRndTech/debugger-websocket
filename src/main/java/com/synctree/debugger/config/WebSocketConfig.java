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
        		.setAllowedOriginPatterns("*") //setAllowedOrigins 지원안함 //TO-DO: !CORS 적용할 URL 패턴 와일드카드에서 특정 URL로 변경
                .withSockJS(); //sockjs 지원
        registry.addHandler(webSocketHandler, "/api/websock")
        		.setAllowedOrigins("*"); // websocket 지원(WS 프로토콜 전용)
    }

}