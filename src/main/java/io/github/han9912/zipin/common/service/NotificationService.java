package io.github.han9912.zipin.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendAsyncDeleteNotice(Long jobId, Long recruiterId) {
        try {
            Thread.sleep(100); // 模拟 I/O 开销
            logger.info("[异步通知] 已删除职位 {} by recruiter {}", jobId, recruiterId);
        } catch (InterruptedException e) {
            logger.error("异步任务失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendApplicationConfirmation(Long userId, Long jobId) {
        try {
            Thread.sleep(100); // 模拟通知过程
            logger.info("[异步通知] 用户 {} 已成功投递职位 {}", userId, jobId);
        } catch (InterruptedException e) {
            logger.error("异步投递确认失败: {}", e.getMessage());
        }
    }

    @Async
    public void sendChatDeliveryLog(Long fromUserId, Long toUserId, String content) {
        try {
            Thread.sleep(50); // 模拟聊天记录落库/通知
            logger.debug("[异步聊天日志] {} -> {}: {}", fromUserId, toUserId, content);
        } catch (InterruptedException e) {
            logger.warn("异步聊天日志失败: {}", e.getMessage());
        }
    }
}
