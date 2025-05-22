package io.github.han9912.zipin.message.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Message {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private final LocalDateTime sentAt = LocalDateTime.now();
}
