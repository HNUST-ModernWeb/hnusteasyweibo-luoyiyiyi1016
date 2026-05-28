package com.weibo.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CommentLikeRecordMapper {
    int count(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    void insert(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
    void delete(@Param("commentId") Integer commentId, @Param("userId") Integer userId);
}
