package com.weibo.mapper;

import com.weibo.entity.Weibo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface WeiboMapper {
    List<Weibo> selectAll();
    List<Weibo> selectByUserId(@Param("userId") Integer userId);
    List<Weibo> selectHot();
    List<Weibo> searchByContent(@Param("q") String q);
    int getTotalLikesByUser(@Param("userId") Integer userId);
    void insert(Weibo weibo);
    void incrementLike(@Param("id") Integer id);
    void decrementLike(@Param("id") Integer id);
    void deleteById(@Param("id") Integer id);
}