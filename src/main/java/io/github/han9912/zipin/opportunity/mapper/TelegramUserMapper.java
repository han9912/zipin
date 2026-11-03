package io.github.han9912.zipin.opportunity.mapper;

import io.github.han9912.zipin.opportunity.entity.TelegramUser;
import org.apache.ibatis.annotations.Param;

public interface TelegramUserMapper {
    void upsert(TelegramUser user);

    TelegramUser findByTelegramId(@Param("telegramId") String telegramId);

    TelegramUser findById(@Param("id") Long id);
}
