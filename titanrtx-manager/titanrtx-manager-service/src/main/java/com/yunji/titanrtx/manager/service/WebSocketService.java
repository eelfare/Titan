package com.yunji.titanrtx.manager.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.ConcurrentHashSet;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Set;

@Slf4j
@Component
@ServerEndpoint("/status")
public class WebSocketService {


    private static final Set<Session> sessionsSet = new ConcurrentHashSet<>();

    @OnMessage
    public void onMessage(Session session, String message) {
        log.debug("webSocket接收到消息：sessionId:{} msg:{}...............................", session.getId(), message);
        if ("TITAN_PING".equals(message)) {
            try {
                session.getBasicRemote().sendText("TITAN_PONG");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @OnOpen
    public void onOpen(Session session) {
        log.debug("webSocket接收到连接：sessionId:{}...............................", session.getId());
        sessionsSet.add(session);
    }

    @OnClose
    public void onClose(Session session) {
        log.debug("webSocket断开连接：sessionId{}..............................................", session.getId());
        sessionsSet.remove(session);
    }


    @OnError
    public void onError(Session session, Throwable error) {
        sessionsSet.remove(session);
        error.printStackTrace();
    }

    private static void sendSessionMessage(String msg) {
        sessionsSet.stream().filter(session -> (session != null && session.isOpen())).forEach(session -> {
            try {
                session.getBasicRemote().sendText(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void sendMessage(String msg) {
        sendSessionMessage(msg);
    }

}
