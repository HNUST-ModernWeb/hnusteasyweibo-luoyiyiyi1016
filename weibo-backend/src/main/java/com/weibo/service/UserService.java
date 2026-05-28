package com.weibo.service;

import com.weibo.entity.User;
import com.weibo.mapper.UserMapper;
import com.weibo.mapper.WeiboMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeiboMapper weiboMapper;

    public User login(User user) { return userMapper.login(user); }
    public void register(User user) { userMapper.register(user); }
    public User getById(Integer id) { return userMapper.getById(id); }
    public void updateById(User user) { userMapper.updateById(user); }
    public List<User> searchByUsername(String q) { return userMapper.searchByUsername(q); }
    public int getTotalLikes(Integer userId) { return weiboMapper.getTotalLikesByUser(userId); }
}