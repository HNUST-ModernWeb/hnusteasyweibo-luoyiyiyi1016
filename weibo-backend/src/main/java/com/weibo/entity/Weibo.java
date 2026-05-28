package com.weibo.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Weibo {
    private Integer id;
    private Integer userId;
    private String content;
    private String imgUrl;
    private Integer likeCount;
    private LocalDateTime createTime;
    private String username;
    private String avatar;
    private Boolean isLiked;
}