package com.weibo.mapper;

import com.weibo.entity.LikeRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LikeRecordMapper {
    LikeRecord find(@Param("weiboId") Integer weiboId, @Param("userId") Integer userId);
    void insert(LikeRecord record);
    void delete(@Param("weiboId") Integer weiboId, @Param("userId") Integer userId);
}
