package io.github.han9912.zipin.resumeprofile.entity;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class ResumeProfile {
    private Long id;

    private Long userId;
    private String title;

    private List<Map<String, String>> education;

    private List<Map<String, String>> experience;

    private List<String> skills;

    private LocalDateTime createdAt = LocalDateTime.now();

}
