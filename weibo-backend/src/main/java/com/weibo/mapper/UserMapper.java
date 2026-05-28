package com.weibo.mapper;

import com.weibo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserMapper {
    User login(User user);
    void register(User user);
    User getById(Integer id);
    void updateById(User user);
    List<User> searchByUsername(@Param("q") String q);
}