package com.weibo.controller;

import com.weibo.entity.User;
import com.weibo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public User login(@RequestBody User user) {
        return userService.login(user);
    }

    @PostMapping("/register")
    public String register(@RequestBody User user) {
        userService.register(user);
        return "注册成功";
    }

    @GetMapping("/info/{id}")
    public Map<String, Object> info(@PathVariable Integer id) {
        User user = userService.getById(id);
        Map<String, Object> map = new HashMap<>();
        if (user != null) {
            map.put("id", user.getId());
            map.put("username", user.getUsername());
            map.put("avatar", user.getAvatar());
            map.put("intro", user.getIntro());
            map.put("gender", user.getGender());
            map.put("birthday", user.getBirthday());
            map.put("email", user.getEmail());
            map.put("location", user.getLocation());
            map.put("createTime", user.getCreateTime());
        }
        map.put("totalLikes", userService.getTotalLikes(id));
        return map;
    }

    @GetMapping("/search")
    public List<User> search(@RequestParam String q) {
        return userService.searchByUsername(q);
    }

    @PostMapping("/update")
    public String update(@RequestBody User user) {
        userService.updateById(user);
        return "success";
    }

    // ====================== 上传头像 ======================
    @PostMapping("/avatar")
    public Map<String, String> avatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam Integer userId) throws IOException {

        String path = "D:/weiboImg/avatar/";
        File dir = new File(path);
        if (!dir.exists()) dir.mkdirs();

        String name = System.currentTimeMillis() + ".jpg";
        file.transferTo(new File(path + name));

        String url = "http://localhost:8080/img/avatar/" + name;

        User u = new User();
        u.setId(userId);
        u.setAvatar(url);
        userService.updateById(u);

        Map<String, String> map = new HashMap<>();
        map.put("url", url);
        return map;
    }
}