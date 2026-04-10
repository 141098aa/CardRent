package com.example.entity.user;

import com.example.entity.Account;
import com.example.entity.UserDriverLicenseAuth;
import com.example.entity.UserRealNameAuth;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class User extends Account {
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
    private Integer status;  // 0:禁用 1:启用
    private LocalDateTime createTime;         // 创建时间
    private LocalDateTime updateTime;         // 更新时间

    // 关联的认证详情对象（用于展示详细认证信息）
    private UserRealNameAuth realNameAuth;        // 实名认证详情
    private UserDriverLicenseAuth driverLicenseAuth; // 驾驶证认证详情

    // 构造函数
    public User() {}
}
