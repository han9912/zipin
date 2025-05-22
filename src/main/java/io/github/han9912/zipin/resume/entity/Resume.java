package io.github.han9912.zipin.resume.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Resume {
    private Long id;

    private Long userId;
    private String fileUrl;
    private String originalName;
    private LocalDateTime createdAt = LocalDateTime.now();
}
