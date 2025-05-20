package io.github.han9912.zipin.user.mapper;

import io.github.han9912.zipin.user.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    int insert(User user);
    User findByEmail(@Param("email") String email);

}
