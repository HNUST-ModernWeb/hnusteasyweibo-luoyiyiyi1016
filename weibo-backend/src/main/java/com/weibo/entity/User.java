package com.weibo.entity;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Integer id;
    private String username;
    private String password;
    private LocalDateTime createTime;
    private String avatar;
    private String intro;
    private String gender;
    private String birthday;
    private String email;
    private String location;
    private Integer followerCount;
}