package io.github.han9912.zipin.user.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class User {
    private Long id;

    private String name;
    private String email;
    private String password;

    private Role role;

    private LocalDateTime createdAt = LocalDateTime.now();
}
