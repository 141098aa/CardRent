package com.example.entity.finance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class DepositRecord {
    private Integer id;
    private Integer orderId;
    private String orderNo;
    private Integer userId;
    private String userName;
    private BigDecimal amount;
    private String status;  // frozen/unfrozen/deducted
    private BigDecimal deductAmount;
    private String deductReason;
    private LocalDateTime deductTime;
    private LocalDateTime unfrozenTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getStatusText() {
        switch (this.status) {
            case "frozen": return "已冻结";
            case "unfrozen": return "已解冻";
            case "deducted": return "已扣除";
            default: return this.status;
        }
    }
}