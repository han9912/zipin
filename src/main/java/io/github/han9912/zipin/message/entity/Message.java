package io.github.han9912.zipin.message.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private final LocalDateTime sentAt = LocalDateTime.now();
}
