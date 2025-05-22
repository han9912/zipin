package io.github.han9912.zipin.message.service;

import io.github.han9912.zipin.common.service.NotificationService;
import io.github.han9912.zipin.message.entity.Message;
import io.github.han9912.zipin.message.mapper.MessageMapper;
//import io.github.han9912.zipin.message.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {
//    @Autowired
//    MessageRepository repo;
    @Autowired
    NotificationService notificationService;
    @Autowired
    MessageMapper mapper;

    public void save(Message message) {
        //repo.save(message);
        mapper.insert(message);
        notificationService.sendChatDeliveryLog(message.getFromUserId(), message.getToUserId(), message.getContent());
    }

    public List<Message> getChatHistory(Long userId1, Long userId2) {
        return mapper.findByIds(userId1, userId2);
        //return repo.findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(userId1, userId2, userId2, userId1);
    }
}
