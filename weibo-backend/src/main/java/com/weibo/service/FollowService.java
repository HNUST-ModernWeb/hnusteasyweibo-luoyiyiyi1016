package com.weibo.service;

import com.weibo.entity.Follow;
import com.weibo.mapper.FollowMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FollowService {
    @Autowired
    private FollowMapper followMapper;

    public void follow(Follow follow) {
        followMapper.follow(follow);
    }

    public void unfollow(Integer followerId, Integer followingId) {
        followMapper.unfollow(followerId, followingId);
    }

    public boolean isFollowing(Integer followerId, Integer followingId) {
        return followMapper.check(followerId, followingId) != null;
    }

    public int countFollowers(Integer userId) {
        return followMapper.countFollowers(userId);
    }

    public int countFollowing(Integer userId) {
        return followMapper.countFollowing(userId);
    }

    public List<Integer> getFollowerIds(Integer userId) {
        return followMapper.getFollowerIds(userId);
    }

    public List<Integer> getFollowingIds(Integer userId) {
        return followMapper.getFollowingIds(userId);
    }

    public List<Integer> getRecommendUserIds(Integer userId) {
        return followMapper.getRecommendUserIds(userId);
    }

    public List<com.weibo.entity.User> getFollowers(Integer userId) {
        return followMapper.getFollowers(userId);
    }

    public List<com.weibo.entity.User> getFollowing(Integer userId) {
        return followMapper.getFollowing(userId);
    }
}
