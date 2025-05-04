package io.github.han9912.zipin.message.repository;

import io.github.han9912.zipin.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByFromUserIdAndToUserIdOrToUserIdAndFromUserId(Long from1, Long to1, Long from2, Long to2);
}
