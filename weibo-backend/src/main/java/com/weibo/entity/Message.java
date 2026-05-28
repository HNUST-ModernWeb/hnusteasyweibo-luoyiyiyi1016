package com.weibo.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Integer id;
    private Integer senderId;
    private Integer receiverId;
    private String content;
    private LocalDateTime createTime;
    private Integer isRead;
    private String senderName;
    private String senderAvatar;
}
