package com.example.entity.user;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Message {
    private Integer id;
    private Integer userId;
    private String title;
    private String content;
    private String type;      // system/refund/order/deposit
    private Boolean isRead;
    private LocalDateTime readTime;
    private String link;
    private LocalDateTime createTime;

    public String getTypeText() {
        switch (this.type) {
            case "system": return "系统通知";
            case "refund": return "退款通知";
            case "order": return "订单通知";
            case "deposit": return "押金通知";
            default: return this.type;
        }
    }
}
