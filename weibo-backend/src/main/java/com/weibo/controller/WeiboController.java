package com.weibo.controller;

import com.weibo.entity.Weibo;
import com.weibo.service.WeiboService;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/weibo")
public class WeiboController {

    @Resource
    private WeiboService weiboService;

    @GetMapping("/listAll")
    public List<Weibo> listAll() {
        return weiboService.listAll();
    }

    @GetMapping("/list")
    public List<Weibo> list(@RequestParam(required = false) Integer userId) {
        if (userId != null) return weiboService.listByUserId(userId);
        return weiboService.listAll();
    }

    @GetMapping("/hot")
    public List<Weibo> hot() {
        return weiboService.listHot();
    }

    @GetMapping("/search")
    public List<Weibo> search(@RequestParam String q) {
        return weiboService.searchByContent(q);
    }

    @GetMapping("/topics")
    public List<Map<String, Object>> topics() {
        return weiboService.getTopTopics(10);
    }

    @PostMapping("/add")
    public void add(@RequestBody Weibo weibo) {
        weiboService.add(weibo);
    }

    @PostMapping("/like/{id}")
    public boolean like(@PathVariable Integer id, @RequestParam Integer userId) {
        return weiboService.toggleLike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        weiboService.delete(id);
    }
}