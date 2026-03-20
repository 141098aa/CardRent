package com.example.entity.finance;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeRecord {
    private Integer id;
    private String rechargeNo;
    private Integer userId;
    private String userName;
    private BigDecimal amount;
    private String paymentMethod;  // wechat/alipay/unionpay
    private String status;          // pending/success/failed
    private LocalDateTime paymentTime;
    private LocalDateTime expireTime;   // 过期时间
    private Boolean canCancel;          // 是否可取消
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 状态文本
    public String getStatusText() {
        switch (this.status) {
            case "pending": return "处理中";
            case "success": return "成功";
            case "failed": return "失败";
            case "cancelled": return "已取消";
            default: return this.status;
        }
    }

    // 状态标签类型
    public String getStatusType() {
        switch (this.status) {
            case "pending": return "warning";
            case "success": return "success";
            case "failed": return "danger";
            case "cancelled": return "info";
            default: return "info";
        }
    }
}
