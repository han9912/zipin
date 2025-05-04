package io.github.han9912.zipin.message.service;

import io.github.han9912.zipin.message.entity.Message;

import java.util.List;

public interface MessageService {
    void save(Message message);
    List<Message> getChatHistory(Long userId1, Long userId2);
}
