package io.github.han9912.zipin.job.mapper;

import io.github.han9912.zipin.job.entity.Job;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface JobMapper {
    int insert(Job job);
    List<Job> search(String keyword);
    Job findById(Long id);
    int delete(Long id);
    List<Job> findAllById(@Param("ids") List<Long> ids);
}
