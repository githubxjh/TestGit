package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("customer")  // 指定表名
public class Customer {
  @TableId(value = "customer_id", type = IdType.AUTO)
  private long customerId;

  private String customerName;
  private String customerPassword;
  private long customerSex;
  private String customerNumber;
  private String customerMailbox;
  private long customerLevel;
  private java.sql.Timestamp gmtCreate;
  private java.sql.Timestamp gmtModify;
  private long customerStatus;

  // 所有的 getter 和 setter 方法保持不变
  public long getCustomerId() {
    return customerId;
  }

  public void setCustomerId(long customerId) {
    this.customerId = customerId;
  }

  public String getCustomerName() {
    return customerName;
  }

  public void setCustomerName(String customerName) {
    this.customerName = customerName;
  }

  public String getCustomerPassword() {
    return customerPassword;
  }

  public void setCustomerPassword(String customerPassword) {
    this.customerPassword = customerPassword;
  }

  public long getCustomerSex() {
    return customerSex;
  }

  public void setCustomerSex(long customerSex) {
    this.customerSex = customerSex;
  }

  public String getCustomerNumber() {
    return customerNumber;
  }

  public void setCustomerNumber(String customerNumber) {
    this.customerNumber = customerNumber;
  }

  public String getCustomerMailbox() {
    return customerMailbox;
  }

  public void setCustomerMailbox(String customerMailbox) {
    this.customerMailbox = customerMailbox;
  }

  public long getCustomerLevel() {
    return customerLevel;
  }

  public void setCustomerLevel(long customerLevel) {
    this.customerLevel = customerLevel;
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

  public long getCustomerStatus() {
    return customerStatus;
  }

  public void setCustomerStatus(long customerStatus) {
    this.customerStatus = customerStatus;
  }
}