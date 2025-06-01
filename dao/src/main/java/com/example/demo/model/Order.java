package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import org.springframework.stereotype.Service;

@Service
@TableName("`order`")
public class Order {
  @TableId("order_id")
  private long orderId;
  private long carId;
  private long customerId;
  private double orderCost;
  private String comments;
  private java.sql.Timestamp gmtCreate;
  private java.sql.Timestamp gmtModify;


  public long getOrderId() {
    return orderId;
  }

  public void setOrderId(long orderId) {
    this.orderId = orderId;
  }


  public long getCarId() {
    return carId;
  }

  public void setCarId(long carId) {
    this.carId = carId;
  }


  public long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(long customerId) {
    this.customerId = customerId;
  }


  public double getOrderCost() {
    return orderCost;
  }

  public void setOrderCost(double orderCost) {
    this.orderCost = orderCost;
  }


  public String getComments() {
    return comments;
  }

  public void setComments(String comments) {
    this.comments = comments;
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

  public Order() {
  }

  @Override
  public String toString() {
    return "Order{" +
            "orderId=" + orderId +
            ", carId=" + carId +
            ", customerId=" + customerId +
            ", orderCost=" + orderCost +
            ", comments='" + comments + '\'' +
            ", gmtCreate=" + gmtCreate +
            ", gmtModify=" + gmtModify +
            '}';
  }
}
