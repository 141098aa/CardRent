package com.example.entity.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AuthAuditDTO {
    private Integer id;
    private Integer userId;
    private String username;
    private String userRealName;  // 用户表中的姓名
    private String phone;
    private String authType;      // 认证类型：real_name 或 driver_license
    private String authName;      // 认证姓名
    private String authNumber;    // 证件号码
    private String frontImage;    // 正面图片
    private String backImage;     // 背面图片
    private Integer status;       // 状态
    private String auditRemark;   // 审核备注
    private LocalDateTime auditTime; // 审核时间
    private LocalDateTime createTime; // 申请时间

    // 驾驶证特有字段
    private String vehicleType;   // 准驾车型
    private LocalDate validStart; // 有效期开始
    private LocalDate validEnd;   // 有效期结束

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    public String getAuthNumber() {
        return authNumber;
    }

    public void setAuthNumber(String authNumber) {
        this.authNumber = authNumber;
    }

    public String getFrontImage() {
        return frontImage;
    }

    public void setFrontImage(String frontImage) {
        this.frontImage = frontImage;
    }

    public String getBackImage() {
        return backImage;
    }

    public void setBackImage(String backImage) {
        this.backImage = backImage;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getAuditRemark() {
        return auditRemark;
    }

    public void setAuditRemark(String auditRemark) {
        this.auditRemark = auditRemark;
    }

    public LocalDateTime getAuditTime() {
        return auditTime;
    }

    public void setAuditTime(LocalDateTime auditTime) {
        this.auditTime = auditTime;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDate getValidStart() {
        return validStart;
    }

    public void setValidStart(LocalDate validStart) {
        this.validStart = validStart;
    }

    public LocalDate getValidEnd() {
        return validEnd;
    }

    public void setValidEnd(LocalDate validEnd) {
        this.validEnd = validEnd;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
