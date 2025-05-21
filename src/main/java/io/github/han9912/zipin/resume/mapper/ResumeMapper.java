package io.github.han9912.zipin.resume.mapper;

import io.github.han9912.zipin.resume.entity.Resume;

import java.util.List;

public interface ResumeMapper {
    int insert(Resume resume);
    List<Resume> findByUserId(Long userId);
    Resume findById(Long id);
    int delete(Long id);
}
