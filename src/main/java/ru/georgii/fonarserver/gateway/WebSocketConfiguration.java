package ru.georgii.fonarserver.gateway;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfiguration implements WebSocketConfigurer {

    @Autowired
    MessageGateway gateway;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
//        webSocketHandlerRegistry.addHandler(new WebSocketHandler() {
//
//            @Override
//            public void afterConnectionEstablished(WebSocketSession session) throws Exception {
//                gateway.
//            }
//
//            @Override
//            public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//
//            }
//
//            @Override
//            public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
//
//            }
//
//            @Override
//            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
//
//            }
//
//            @Override
//            public boolean supportsPartialMessages() {
//                return false;
//            }
//        }, "/socket.io/*");

        webSocketHandlerRegistry.addHandler(new SpringWebSocketHandler(gateway.mEngineIoServer), "/socket.io/*");
    }
}
