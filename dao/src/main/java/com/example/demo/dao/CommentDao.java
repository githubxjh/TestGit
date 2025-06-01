package com.example.demo.dao;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.demo.mapper.CommentMapper;
import com.example.demo.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class CommentDao {

    @Autowired
    private CommentMapper commentMapper;

    public List<Comment> getAllCommentsByStatus(Integer status) {
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("comment_status", status);  // 查找状态为指定值的评论
        return commentMapper.selectList(commentQueryWrapper);
    }

    public List<Comment> getAllCommentsByStatusAndConditions(Integer status, String datemin, String datemax,
                                                             String searchKeyword, Long roomId, Long customerId) {
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("comment_status", status);

        if (datemin != null && !datemin.isEmpty()) {
            commentQueryWrapper.ge("gmt_create", datemin);
        }
        if (datemax != null && !datemax.isEmpty()) {
            commentQueryWrapper.le("gmt_create", datemax);
        }
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            commentQueryWrapper.and(wrapper -> wrapper
                    .like("comment_id", searchKeyword)
                    .or()
                    .like("comment_content", searchKeyword)
                    .or()
                    .like("order_id", searchKeyword));
        }
        if (roomId != null) {
            commentQueryWrapper.eq("room_id", roomId);
        }
        if (customerId != null) {
            commentQueryWrapper.eq("customer_id", customerId);
        }

        return commentMapper.selectList(commentQueryWrapper);
    }

    public Comment getCommentById(Long commentId) {
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("comment_id", commentId);
        return commentMapper.selectOne(commentQueryWrapper);
    }

    public void updateComment(Comment comment) {
        // 使用更新条件而不是updateById
        QueryWrapper<Comment> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("comment_id", comment.getCommentId());
        commentMapper.update(comment, queryWrapper);
    }

    public Integer offlineComment(Long commentId) {
        QueryWrapper<Comment> commentQueryWrapper = new QueryWrapper<>();
        commentQueryWrapper.eq("comment_id", commentId);
        Comment comment = commentMapper.selectOne(commentQueryWrapper);
        if (comment == null) {
            System.out.println("Comment with id " + commentId + " not found.");
            return 0;
        }
        comment.setCommentStatus(0);
        Timestamp currentTimestamp = Timestamp.valueOf(DateUtil.date().toString("yyyy-MM-dd HH:mm:ss"));
        comment.setGmtModify(currentTimestamp);
        int result = commentMapper.update(comment, commentQueryWrapper);
        System.out.println("Rows affected: " + result);
        return result;
    }

    public void addComment(Comment comment) {
        commentMapper.insert(comment);
    }

    public Integer deleteComment(Long commentId) {
        return commentMapper.deleteById(commentId);
    }
}