package com.weibo.mapper;

import com.weibo.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FollowMapper {
    void follow(Follow follow);
    void unfollow(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);
    Follow check(@Param("followerId") Integer followerId, @Param("followingId") Integer followingId);
    int countFollowers(@Param("userId") Integer userId);
    int countFollowing(@Param("userId") Integer userId);
    List<Integer> getFollowerIds(@Param("userId") Integer userId);
    List<Integer> getFollowingIds(@Param("userId") Integer userId);
    List<Integer> getRecommendUserIds(@Param("userId") Integer userId);
    List<com.weibo.entity.User> getFollowers(@Param("userId") Integer userId);
    List<com.weibo.entity.User> getFollowing(@Param("userId") Integer userId);
}
