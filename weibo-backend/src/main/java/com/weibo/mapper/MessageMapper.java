package com.weibo.mapper;

import com.weibo.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MessageMapper {
    void send(Message msg);
    List<Message> getConversation(@Param("user1") Integer user1, @Param("user2") Integer user2);
    List<Message> getConversations(@Param("userId") Integer userId);
    int getUnreadCount(@Param("userId") Integer userId);
    void markAsRead(@Param("user1") Integer user1, @Param("user2") Integer user2);
}
