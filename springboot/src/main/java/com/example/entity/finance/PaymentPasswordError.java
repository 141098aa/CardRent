package com.example.entity.finance;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PaymentPasswordError {
    private Integer id;
    private Integer userId;
    private Integer errorCount;
    private Integer totalErrorCount;
    private LocalDateTime firstErrorTime;
    private LocalDateTime lockStartTime;
    private LocalDateTime lockEndTime;
    private String lockType;  // short/long
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
