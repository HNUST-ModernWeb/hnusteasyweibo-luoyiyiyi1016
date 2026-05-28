package com.weibo.controller;

import com.weibo.entity.Comment;
import com.weibo.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/comment")
public class CommentController {
    @Autowired
    private CommentService commentService;

    @GetMapping("/list/{weiboId}")
    public List<Comment> list(@PathVariable Integer weiboId) {
        return commentService.list(weiboId);
    }

    @PostMapping("/add")
    public void add(@RequestBody Comment comment) {
        commentService.add(comment);
    }

    @PostMapping("/like/{id}")
    public boolean like(@PathVariable Integer id, @RequestParam Integer userId) {
        return commentService.toggleLike(id, userId);
    }
}