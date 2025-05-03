package io.github.han9912.zipin.resumeprofile.entity;

import io.github.han9912.zipin.common.util.JsonListConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "resume_profiles")
@Setter
@Getter
public class ResumeProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;

    @Convert(converter = JsonListConverter.class)
    private List<Map<String, String>> education;

    @Convert(converter = JsonListConverter.class)
    private List<Map<String, String>> experience;

    @Convert(converter = JsonListConverter.class)
    private List<String> skills;

    private LocalDateTime createdAt = LocalDateTime.now();

    // getters & setters
}
