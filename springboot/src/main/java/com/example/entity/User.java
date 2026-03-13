package com.example.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User extends Account{
    private Integer id;
    private String username;
    private String password;
    private String name;
    private String avatar;
    private String role;
    private BigDecimal account;

    private String phone;                    // 手机号
    private String email;                    // 邮箱
    private String paymentPassword;           // 支付密码
    private Integer realNameVerified;         // 实名认证状态(0:未认证 1:已认证 2:审核失败)
    private Integer driverLicenseVerified;    // 驾驶证认证状态(0:未认证 1:已认证 2:审核失败)
    private LocalDateTime createTime;         // 创建时间
    private LocalDateTime updateTime;         // 更新时间

    // 可选：关联的认证详情对象（用于需要展示详细认证信息的场景）
    private UserRealNameAuth realNameAuth;        // 实名认证详情
    private UserDriverLicenseAuth driverLicenseAuth; // 驾驶证认证详情

    // 构造函数
    public User() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getAccount() {
        return account;
    }

    public void setAccount(BigDecimal account) {
        this.account = account;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPaymentPassword() {
        return paymentPassword;
    }

    public void setPaymentPassword(String paymentPassword) {
        this.paymentPassword = paymentPassword;
    }

    public Integer getRealNameVerified() {
        return realNameVerified;
    }

    public void setRealNameVerified(Integer realNameVerified) {
        this.realNameVerified = realNameVerified;
    }

    public Integer getDriverLicenseVerified() {
        return driverLicenseVerified;
    }

    public void setDriverLicenseVerified(Integer driverLicenseVerified) {
        this.driverLicenseVerified = driverLicenseVerified;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public UserRealNameAuth getRealNameAuth() {
        return realNameAuth;
    }

    public void setRealNameAuth(UserRealNameAuth realNameAuth) {
        this.realNameAuth = realNameAuth;
    }

    public UserDriverLicenseAuth getDriverLicenseAuth() {
        return driverLicenseAuth;
    }

    public void setDriverLicenseAuth(UserDriverLicenseAuth driverLicenseAuth) {
        this.driverLicenseAuth = driverLicenseAuth;
    }
}
