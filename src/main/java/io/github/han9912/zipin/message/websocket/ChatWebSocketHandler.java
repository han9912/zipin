package io.github.han9912.zipin.message.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.han9912.zipin.message.entity.Message;
import io.github.han9912.zipin.message.service.MessageService;
import io.github.han9912.zipin.user.util.JwtUtil;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {
    private final Map<Long, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Autowired
    JwtUtil jwtUtil;
    @Autowired
    MessageService messageService;

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String token = getToken(session);
        if (token == null) {
            session.close(CloseStatus.BAD_DATA); // 主动关闭连接
            return;
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            session.close(CloseStatus.BAD_DATA);
            return;
        }
        session.getAttributes().put("userId", userId); // FIX: 你没保存userId，后续handleTextMessage拿不到
        sessions.put(userId, session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long fromId = (Long) session.getAttributes().get("userId");
        Map<String, Object> msg = new ObjectMapper().readValue(message.getPayload(), Map.class);
        Long toId = Long.valueOf(msg.get("to").toString());
        String content = msg.get("content").toString();

        Message m = new Message();
        m.setFromUserId(fromId);
        m.setToUserId(toId);
        m.setContent(content);
        messageService.save(m);

        if (sessions.containsKey(toId)) {
            sessions.get(toId).sendMessage(new TextMessage(new ObjectMapper().writeValueAsString(m)));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        Long userId = (Long) session.getAttributes().get("userId");
        sessions.remove(userId);
    }

    private String getToken(WebSocketSession session) {
        String query = Objects.requireNonNull(session.getUri()).getQuery();
        if (query == null) return null;
        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }
}
