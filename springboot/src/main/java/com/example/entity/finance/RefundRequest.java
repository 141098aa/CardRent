package com.example.entity.finance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RefundRequest {
    private Integer id;
    private String refundNo;
    private Integer orderId;
    private String orderNo;
    private Integer userId;
    private String userName;
    private BigDecimal amount;
    private String reason;
    private String status;  // pending/approved/rejected/completed
    private String auditRemark;
    private LocalDateTime auditTime;
    private LocalDateTime completeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getStatusText() {
        switch (this.status) {
            case "pending": return "待审核";
            case "approved": return "已通过";
            case "rejected": return "已拒绝";
            case "completed": return "已完成";
            default: return this.status;
        }
    }
}