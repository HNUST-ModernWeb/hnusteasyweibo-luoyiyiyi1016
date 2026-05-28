package com.weibo.mapper;

import com.weibo.entity.Comment;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CommentMapper {
    List<Comment> list(@Param("weiboId") Integer weiboId);
    int add(Comment comment);
    void incrementLike(Integer id);
    void decrementLike(Integer id);
}