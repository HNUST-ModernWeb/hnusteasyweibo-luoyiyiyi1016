package com.weibo.entity;
import lombok.Data;

@Data
public class LikeRecord {
    private Integer id;
    private Integer weiboId;
    private Integer userId;
}