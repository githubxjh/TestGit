package com.example.demo.service;

import com.example.demo.dao.CommentDao;
import com.example.demo.model.Comment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentDao commentDao;

    public List<Comment> getAllCommentsByStatus(Integer status) {
        return commentDao.getAllCommentsByStatus(status);
    }

    @Transactional
    public List<Comment> getAllCommentsByStatusAndConditions(Integer status, String datemin, String datemax,
                                                             String searchKeyword, Long roomId, Long customerId) {
        return commentDao.getAllCommentsByStatusAndConditions(status, datemin, datemax,
                searchKeyword, roomId, customerId);
    }

    public Comment getCommentById(Long commentId) {
        return commentDao.getCommentById(commentId);
    }

    @Transactional
    public void updateComment(Comment comment) {
        commentDao.updateComment(comment);
    }

    @Transactional
    public Integer offlineComment(Long commentId) {
        return commentDao.offlineComment(commentId);
    }

    @Transactional
    public void addComment(Comment comment) {
        commentDao.addComment(comment);
    }

    @Transactional
    public Integer deleteComment(Long commentId) {
        return commentDao.deleteComment(commentId);
    }
}