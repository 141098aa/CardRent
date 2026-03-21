package com.example.entity.finance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRecord {
    private Integer id;
    private String transactionNo;
    private Integer userId;
    private String userName;
    private String type;  // recharge/payment/refund/deposit_freeze/deposit_unfreeze/deposit_deduct
    private BigDecimal amount;
    private BigDecimal balance;
    private Integer relatedId;
    private String relatedNo;
    private String remark;
    private LocalDateTime createTime;

    public String getTypeText() {
        switch (this.type) {
            case "recharge": return "充值";
            case "payment": return "支付";
            case "refund": return "退款";
            case "deposit_freeze": return "押金冻结";
            case "deposit_unfreeze": return "押金解冻";
            case "deposit_deduct": return "押金扣除";
            default: return this.type;
        }
    }
}