package com.weibo.service;

import com.weibo.entity.Comment;
import com.weibo.mapper.CommentLikeRecordMapper;
import com.weibo.mapper.CommentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentLikeRecordMapper commentLikeRecordMapper;

    public List<Comment> list(Integer weiboId) {
        return commentMapper.list(weiboId);
    }

    public void add(Comment comment) {
        commentMapper.add(comment);
    }

    @Transactional
    public boolean toggleLike(Integer commentId, Integer userId) {
        int likes = commentLikeRecordMapper.count(commentId, userId);
        if (likes == 0) {
            commentLikeRecordMapper.insert(commentId, userId);
            commentMapper.incrementLike(commentId);
            return true;
        } else {
            commentLikeRecordMapper.delete(commentId, userId);
            commentMapper.decrementLike(commentId);
            return false;
        }
    }
}