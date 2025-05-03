package io.github.han9912.zipin.resume.service;

import io.github.han9912.zipin.resume.entity.Resume;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {
    Resume upload(MultipartFile file, Long userId);
    List<Resume> list(Long userId);
    void delete(Long id, Long userId);
}
