package io.github.han9912.zipin.message.controller;

import io.github.han9912.zipin.common.dto.Result;
import io.github.han9912.zipin.common.util.AuthUtil;
import io.github.han9912.zipin.message.entity.Message;
import io.github.han9912.zipin.message.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    @Autowired
    private MessageService service;
    @Autowired
    AuthUtil authUtil;

    @GetMapping("/{userId}")
    public Result<List<Message>> getChatHistory(@RequestHeader("Authorization") String auth,
                                                @PathVariable Long userId) {
        Long myId = authUtil.resolveUid(auth);
        return Result.ok(service.getChatHistory(myId, userId));
    }
}
