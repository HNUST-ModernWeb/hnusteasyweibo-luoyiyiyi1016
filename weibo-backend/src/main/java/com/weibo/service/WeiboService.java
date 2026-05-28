package com.weibo.service;

import com.weibo.entity.LikeRecord;
import com.weibo.entity.Weibo;
import com.weibo.mapper.LikeRecordMapper;
import com.weibo.mapper.WeiboMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WeiboService {

    @Resource
    private WeiboMapper weiboMapper;

    @Resource
    private LikeRecordMapper likeRecordMapper;

    public List<Weibo> listAll() {
        return weiboMapper.selectAll();
    }

    public List<Weibo> listByUserId(Integer userId) {
        return weiboMapper.selectByUserId(userId);
    }

    public List<Weibo> listHot() {
        return weiboMapper.selectHot();
    }

    public List<Weibo> searchByContent(String q) {
        return weiboMapper.searchByContent(q);
    }

    public void add(Weibo weibo) {
        weiboMapper.insert(weibo);
    }

    @Transactional
    public boolean toggleLike(Integer id, Integer userId) {
        LikeRecord record = likeRecordMapper.find(id, userId);
        if (record == null) {
            LikeRecord r = new LikeRecord();
            r.setWeiboId(id);
            r.setUserId(userId);
            likeRecordMapper.insert(r);
            weiboMapper.incrementLike(id);
            return true;
        } else {
            likeRecordMapper.delete(id, userId);
            weiboMapper.decrementLike(id);
            return false;
        }
    }

    public void delete(Integer id) {
        weiboMapper.deleteById(id);
    }

    public List<Map<String, Object>> getTopTopics(int limit) {
        List<Weibo> all = weiboMapper.selectAll();
        Pattern pattern = Pattern.compile("#([^#]+)#");
        Map<String, Integer> topicCount = new LinkedHashMap<>();
        for (Weibo wb : all) {
            if (wb.getContent() != null) {
                Matcher matcher = pattern.matcher(wb.getContent());
                while (matcher.find()) {
                    String topic = matcher.group(1).trim();
                    if (!topic.isEmpty()) {
                        topicCount.put(topic, topicCount.getOrDefault(topic, 0) + 1);
                    }
                }
            }
        }
        return topicCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .collect(Collectors.toList());
    }
}