package org.example.web.controller;

import com.example.demo.model.Comment;
import com.example.demo.model.Customer;
import com.example.demo.service.CommentService;
import com.example.demo.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.*;

@Controller
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private CustomerService customerService;

    @GetMapping("/comment-list")
    public String getAllComments(Model model,
                                 @RequestParam(value = "datemin", required = false) String datemin,
                                 @RequestParam(value = "datemax", required = false) String datemax,
                                 @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
                                 @RequestParam(value = "roomId", required = false) Long roomId,
                                 @RequestParam(value = "customerId", required = false) Long customerId) {

        List<Comment> commentList;

        // 如果有搜索条件，使用条件查询
        if (datemin != null || datemax != null || searchKeyword != null || roomId != null || customerId != null) {
            commentList = commentService.getAllCommentsByStatusAndConditions(
                    1, datemin, datemax, searchKeyword, roomId, customerId);
        } else {
            // 否则获取所有活跃评论
            commentList = commentService.getAllCommentsByStatus(1);
        }

        model.addAttribute("commentlist", commentList);
        model.addAttribute("total", commentList.size());
        return "comment-list";
    }

    @GetMapping("/comment-search")
    @ResponseBody
    public Map<String, Object> searchComments(
            @RequestParam(value = "datemin", required = false) String datemin,
            @RequestParam(value = "datemax", required = false) String datemax,
            @RequestParam(value = "searchKeyword", required = false) String searchKeyword,
            @RequestParam(value = "roomId", required = false) Long roomId,
            @RequestParam(value = "customerId", required = false) Long customerId) {

        List<Comment> commentList = commentService.getAllCommentsByStatusAndConditions(
                1, datemin, datemax, searchKeyword, roomId, customerId);

        // 简化返回的数据
        List<Map<String, Object>> simplifiedList = new ArrayList<>();
        for (Comment comment : commentList) {
            Map<String, Object> commentMap = new HashMap<>();
            commentMap.put("commentId", comment.getCommentId());
            commentMap.put("orderId", comment.getOrderId());
            commentMap.put("customerId", comment.getCustomerId());
            commentMap.put("roomId", comment.getRoomId());
            commentMap.put("commentContent", comment.getCommentContent());
            commentMap.put("rating", comment.getRating());
            commentMap.put("imageUrl", comment.getImageUrl());
            commentMap.put("gmtCreate", comment.getGmtCreate());
            commentMap.put("gmtModify", comment.getGmtModify());
            commentMap.put("commentStatus", comment.getCommentStatus());

            simplifiedList.add(commentMap);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("commentlist", simplifiedList);
        response.put("total", commentList.size());
        return response;
    }

    @GetMapping("/offlinecomment/{commentid}")
    public String getOfflineCommentById(@PathVariable("commentid") Long commentid, Model model) {
        Comment comment = commentService.getCommentById(commentid);
        model.addAttribute("comment", comment);
        return "offlinecomment";
    }

    @GetMapping("/editcomment/{commentid}")
    public String editCommentById(@PathVariable("commentid") Long commentid, Model model) {
        Comment comment = commentService.getCommentById(commentid);
        model.addAttribute("comment", comment);
        return "editcomment";
    }

    @PostMapping("/updateComment")
    public String updateComment(
            @RequestParam("commentId") Long commentId,
            @RequestParam("orderId") Long orderId,
            @RequestParam("customerId") Long customerId,
            @RequestParam("roomId") Long roomId,
            @RequestParam("commentContent") String commentContent,
            @RequestParam("rating") Long rating,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam("commentStatus") Long commentStatus) {

        Comment comment = commentService.getCommentById(commentId);
        if (comment == null) {
            return "redirect:/error";
        }

        comment.setOrderId(orderId);
        comment.setCustomerId(customerId);
        comment.setRoomId(roomId);
        comment.setCommentContent(commentContent);
        comment.setRating(rating);
        comment.setGmtModify(new Timestamp(System.currentTimeMillis()));

        // 处理图片上传
        if (imageFile != null && !imageFile.isEmpty()) {
            String newImagePath = saveImage(imageFile);
            if (newImagePath != null) {
                comment.setImageUrl(newImagePath);
            }
        }

        commentService.updateComment(comment);
        return "redirect:/success";
    }

    @GetMapping("/offlinecomment/offline_confirm")
    public String offlineConfirm(@RequestParam("commentid") Long commentid) {
        System.out.println("offline_confirm:" + commentid);
        int result = commentService.offlineComment(commentid);
        System.out.println("Update result: " + result);
        if (result == 1) {
            return "redirect:/success";
        } else {
            return "redirect:/error";
        }
    }

    @GetMapping("/comment-add")
    public String getCommentAddPage() {
        return "comment-add";
    }

    @PostMapping("/addComment")
    @ResponseBody
    public Map<String, Object> addComment(
            @RequestParam("orderId") Long orderId,
            @RequestParam("customerId") Long customerId,
            @RequestParam("roomId") Long roomId,
            @RequestParam("commentContent") String commentContent,
            @RequestParam("rating") Long rating,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 创建新的Comment对象
            Comment comment = new Comment();
            comment.setOrderId(orderId);
            comment.setCustomerId(customerId);
            comment.setRoomId(roomId);
            comment.setCommentContent(commentContent);
            comment.setRating(rating);

            // 处理图片上传
            if (imageFile != null && !imageFile.isEmpty()) {
                String imagePath = saveImage(imageFile);
                comment.setImageUrl(imagePath);
            } else {
                comment.setImageUrl(""); // 默认无图片
            }

            // 设置创建时间和修改时间
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            comment.setGmtCreate(currentTime);
            comment.setGmtModify(currentTime);

            // 设置评论状态为活跃(1)
            comment.setCommentStatus(1);

            // 保存评论
            commentService.addComment(comment);

            response.put("code", 0);
            response.put("msg", "添加评论成功");
        } catch (Exception e) {
            response.put("code", 1);
            response.put("msg", "添加评论失败: " + e.getMessage());
        }

        return response;
    }

    @PostMapping("/deleteComment")
    @ResponseBody
    public Map<String, Object> deleteComment(@RequestParam("commentid") Long commentid) {
        Map<String, Object> response = new HashMap<>();
        try {
            int result = commentService.deleteComment(commentid);
            if (result > 0) {
                response.put("code", 0);
                response.put("msg", "删除成功");
            } else {
                response.put("code", 1);
                response.put("msg", "删除失败，评论不存在");
            }
        } catch (Exception e) {
            response.put("code", 1);
            response.put("msg", "删除失败: " + e.getMessage());
        }
        return response;
    }

    // 辅助方法：保存上传的图片
    private String saveImage(MultipartFile file) {
        try {
            // 获取文件名
            String originalFilename = file.getOriginalFilename();
            // 生成新文件名
            String newFilename = UUID.randomUUID().toString() +
                    originalFilename.substring(originalFilename.lastIndexOf("."));

            // 确定文件保存路径
            String uploadDir = "src/main/resources/static/uploads/comments/";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 保存文件
            File dest = new File(dir.getAbsolutePath() + File.separator + newFilename);
            file.transferTo(dest);

            // 返回可访问的相对路径
            return "/static/uploads/comments/" + newFilename;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    @GetMapping("/viewcustomer/{customerid}")
    public String viewCustomerById(@PathVariable("customerid") Long customerid, Model model) {
        Customer customer = customerService.getCustomerById(customerid);
        model.addAttribute("customer", customer);
        return "viewcustomer";  // 返回只读的用户详情页面
    }
}