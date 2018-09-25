package com.divroll.backend;

import org.atmosphere.config.service.WebSocketHandlerService;
import org.atmosphere.util.SimpleBroadcaster;
import org.atmosphere.websocket.WebSocket;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.atmosphere.websocket.WebSocketHandler;
import org.atmosphere.websocket.WebSocketProcessor;

import java.io.IOException;

//@WebSocketHandlerService(path = "/chat", broadcaster = SimpleBroadcaster.class)
public class DivrollWebsocketHandler implements WebSocketHandler {
    @Override
    public void onByteMessage(WebSocket webSocket, byte[] bytes, int i, int i1) throws IOException {
        System.out.println("onByteMessage");
    }

    @Override
    public void onTextMessage(WebSocket webSocket, String s) throws IOException {
        System.out.println("onTextMessage - " + s);

    }

    @Override
    public void onOpen(WebSocket webSocket) throws IOException {
        System.out.println("onOpen");
        webSocket.resource().addEventListener(new WebSocketEventListenerAdapter(){
            @Override
            public void onDisconnect(WebSocketEvent event) {
                System.out.println("onDisconnect");
            }
            @Override
            public void onMessage(WebSocketEvent event) {
                webSocket.broadcast("echo - " + event.message());
            }
        });

    }

    @Override
    public void onClose(WebSocket webSocket) {
        System.out.println("onClose");

    }

    @Override
    public void onError(WebSocket webSocket, WebSocketProcessor.WebSocketException e) {
        System.out.println("onError");

    }
}
