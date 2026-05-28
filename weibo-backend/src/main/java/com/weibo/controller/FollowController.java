package com.weibo.controller;

import com.weibo.entity.Follow;
import com.weibo.service.FollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.weibo.entity.User;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private FollowService followService;

    @PostMapping("/{userId}")
    public String follow(@PathVariable Integer userId, @RequestParam Integer followerId) {
        Follow f = new Follow();
        f.setFollowerId(followerId);
        f.setFollowingId(userId);
        followService.follow(f);
        return "ok";
    }

    @DeleteMapping("/{userId}")
    public String unfollow(@PathVariable Integer userId, @RequestParam Integer followerId) {
        followService.unfollow(followerId, userId);
        return "ok";
    }

    @GetMapping("/check")
    public Map<String, Object> check(@RequestParam Integer followerId, @RequestParam Integer followingId) {
        Map<String, Object> map = new HashMap<>();
        map.put("following", followService.isFollowing(followerId, followingId));
        return map;
    }

    @GetMapping("/count")
    public Map<String, Integer> count(@RequestParam Integer userId) {
        Map<String, Integer> map = new HashMap<>();
        map.put("followers", followService.countFollowers(userId));
        map.put("following", followService.countFollowing(userId));
        return map;
    }

    @GetMapping("/recommend")
    public List<Integer> recommend(@RequestParam Integer userId) {
        return followService.getRecommendUserIds(userId);
    }

    @GetMapping("/followers")
    public List<User> followers(@RequestParam Integer userId) {
        return followService.getFollowers(userId);
    }

    @GetMapping("/following")
    public List<User> following(@RequestParam Integer userId) {
        return followService.getFollowing(userId);
    }
}
