package com.weibo.service;

import com.weibo.entity.Message;
import com.weibo.mapper.MessageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MessageService {
    @Autowired
    private MessageMapper messageMapper;

    public void send(Message msg) {
        messageMapper.send(msg);
    }

    public List<Message> getConversation(Integer user1, Integer user2) {
        return messageMapper.getConversation(user1, user2);
    }

    public List<Message> getConversations(Integer userId) {
        return messageMapper.getConversations(userId);
    }

    public int getUnreadCount(Integer userId) {
        return messageMapper.getUnreadCount(userId);
    }

    public void markAsRead(Integer user1, Integer user2) {
        messageMapper.markAsRead(user1, user2);
    }
}
