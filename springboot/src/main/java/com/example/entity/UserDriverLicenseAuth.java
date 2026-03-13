package com.example.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class UserDriverLicenseAuth {
    private Integer id;
    private Integer userId;
    private String driverName;
    private String licenseNumber;
    private String vehicleType;
    private LocalDate validStart;
    private LocalDate validEnd;
    private String licenseFrontImage;
    private String licenseBackImage;
    private Integer status;  // 0:待审核 1:已认证 2:审核失败
    private String auditRemark;
    private LocalDateTime auditTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
