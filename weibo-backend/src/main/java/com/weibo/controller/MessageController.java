package com.weibo.controller;

import com.weibo.entity.Message;
import com.weibo.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public String send(@RequestBody Message msg) {
        messageService.send(msg);
        return "ok";
    }

    @GetMapping("/conversation")
    public List<Message> conversation(@RequestParam Integer user1, @RequestParam Integer user2) {
        return messageService.getConversation(user1, user2);
    }

    @GetMapping("/conversations")
    public List<Message> conversations(@RequestParam Integer userId) {
        return messageService.getConversations(userId);
    }

    @GetMapping("/unread")
    public int unread(@RequestParam Integer userId) {
        return messageService.getUnreadCount(userId);
    }

    @PostMapping("/read")
    public String markRead(@RequestParam Integer user1, @RequestParam Integer user2) {
        messageService.markAsRead(user1, user2);
        return "ok";
    }
}
