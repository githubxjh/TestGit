package com.example.demo.model;

import com.baomidou.mybatisplus.annotation.TableId;
import org.springframework.stereotype.Service;

@Service
public class Admin {
  @TableId("admin_id")
  private long adminId;
  private String adminName;
  private String adminPassword;
  private String adminPhone;
  private String adminMailbox;
  private long adminLevel;
  private java.sql.Timestamp gmtCreate;
  private long adminStatus;

  public String getAdminPassword() {
    return adminPassword;
  }

  public void setAdminPassword(String adminPassword) {
    this.adminPassword = adminPassword;
  }
  public long getAdminId() {
    return adminId;
  }

  public void setAdminId(long adminId) {
    this.adminId = adminId;
  }


  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }




  public String getAdminPhone() {
    return adminPhone;
  }

  public void setAdminPhone(String adminPhone) {
    this.adminPhone = adminPhone;
  }


  public String getAdminMailbox() {
    return adminMailbox;
  }

  public void setAdminMailbox(String adminMailbox) {
    this.adminMailbox = adminMailbox;
  }


  public long getAdminLevel() {
    return adminLevel;
  }

  public void setAdminLevel(long adminLevel) {
    this.adminLevel = adminLevel;
  }


  public java.sql.Timestamp getGmtCreate() {
    return gmtCreate;
  }

  public void setGmtCreate(java.sql.Timestamp gmtCreate) {
    this.gmtCreate = gmtCreate;
  }


  public long getAdminStatus() {
    return adminStatus;
  }

  public void setAdminStatus(long adminStatus) {
    this.adminStatus = adminStatus;
  }

}
