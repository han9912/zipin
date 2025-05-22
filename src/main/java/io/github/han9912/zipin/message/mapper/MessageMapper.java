package io.github.han9912.zipin.message.mapper;

import io.github.han9912.zipin.message.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MessageMapper {
    int insert(Message message);
    List<Message> findByIds(@Param("uid1")Long uid1, @Param("uid2")Long uid2);
}
