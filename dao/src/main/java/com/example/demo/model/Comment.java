package com.example.demo.model;


public class Comment {

  private long commentId;
  private long orderId;
  private long customerId;
  private long roomId;
  private String commentContent;
  private long rating;
  private String ImageUrl;
  private java.sql.Timestamp gmtCreate;
  private java.sql.Timestamp gmtModify;
  private long commentStatus;


  public long getCommentId() {
    return commentId;
  }

  public void setCommentId(long commentId) {
    this.commentId = commentId;
  }


  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }


  public long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(long customerId) {
    this.customerId = customerId;
  }


  public long getRoomId() {
    return roomId;
  }

  public void setRoomId(long roomId) {
    this.roomId = roomId;
  }


  public String getCommentContent() {
    return commentContent;
  }

  public void setCommentContent(String commentContent) {
    this.commentContent = commentContent;
  }


  public long getRating() {
    return rating;
  }

  public void setRating(long rating) {
    this.rating = rating;
  }


  public String getImageUrl() {
    return ImageUrl;
  }

  public void setImageUrl(String ImageUrl) {
    this.ImageUrl = ImageUrl;
  }


  public java.sql.Timestamp getGmtCreate() {
    return gmtCreate;
  }

  public void setGmtCreate(java.sql.Timestamp gmtCreate) {
    this.gmtCreate = gmtCreate;
  }


  public java.sql.Timestamp getGmtModify() {
    return gmtModify;
  }

  public void setGmtModify(java.sql.Timestamp gmtModify) {
    this.gmtModify = gmtModify;
  }


  public long getCommentStatus() {
    return commentStatus;
  }

  public void setCommentStatus(long commentStatus) {
    this.commentStatus = commentStatus;
  }

}
