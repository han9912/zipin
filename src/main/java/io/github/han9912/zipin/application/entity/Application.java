package io.github.han9912.zipin.application.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class Application {

    private Long id;

    private Long jobId;
    private Long userId;
    private Long resumeId;

    private String profileSnapshot; // 结构化简历快照（JSON）

    private ApplicationStatus status = ApplicationStatus.PENDING;

    private LocalDateTime appliedAt = LocalDateTime.now();

}
