package com.example.entity.finance;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentPasswordReset {
    private Integer id;
    private Integer userId;
    private String resetCode;
    private LocalDateTime expireTime;
    private Boolean used;
    private LocalDateTime createTime;
}