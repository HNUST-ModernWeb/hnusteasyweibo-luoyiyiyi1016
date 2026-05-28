package com.weibo.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Comment {
    private Integer id;
    private Integer weiboId;
    private Integer userId;
    private String content;
    private LocalDateTime createTime;
    private String username;
    private String avatar;
    private Integer parentId;
    private String replyTo;
    private Integer likeCount;
    private Boolean isLiked;
}