package com.example.entity;

import java.time.LocalDateTime;

public class UserRealNameAuth {
    private Integer id;
    private Integer userId;
    private String realName;
    private String idNumber;
    private String idFrontImage;
    private String idBackImage;
    private Integer status;  // 0:待审核 1:已认证 2:审核失败
    private String auditRemark;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
