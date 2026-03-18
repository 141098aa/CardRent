package com.example.entity.order;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Order {
    private Integer id;
    private Integer orderId;
    private String orderNo;
    private Integer userId;
    private String userName;
    private String userPhone;
    private Integer carId;
    private String carName;
    private String carImage;
    private BigDecimal dailyPrice;
    private LocalDateTime pickupTime;
    private LocalDateTime returnTime;
    private Integer days;
    private String pickupLocation;
    private String returnLocation;
    private BigDecimal totalPrice;
    private String paymentMethod;
    private LocalDateTime paymentTime;
    private String status;          // 订单状态
    private String remark;
    private String auditRemark;
    private String cancelReason;
    private Integer carSeats;
    private String carGear;
    private String carEnergy;
    private BigDecimal deposit;  // 押金金额
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String priceAdjustments;       // JSON格式存储所有调价明细
    private BigDecimal dynamicRent;  // 调整后租金

    // 非持久化字段，用于前端展示解析后的调价明细
    private List<PriceAdjustment> adjustmentList;  // 不映射到数据库


    // 内部类定义调价项
    @Data
    public static class PriceAdjustment {
        private String type;        // 调价类型：season/weekend/discount
        private String name;        // 调价名称：节假日溢价/周末溢价/长租折扣
        private BigDecimal factor;   // 系数：1.2, 0.9等
        private String description;  // 描述：周末+20%
        private BigDecimal amount;   // 调价金额
         }

    // 状态文本（用于前端显示）
    public String getStatusText() {
        switch (this.status) {
            case "pending_pay": return "待付款";
            case "pending_audit": return "待审核";
            case "pending_pickup": return "待取车";
            case "active": return "进行中";
            case "completed": return "已完成";
            case "cancelled": return "已取消";
            case "refunding": return "退款中";
            case "refunded": return "已退款";
            default: return this.status;
        }
    }

    // 状态标签类型（用于前端el-tag）
    public String getStatusType() {
        switch (this.status) {
            case "pending_pay":
            case "pending_audit": return "warning";
            case "pending_pickup":
            case "active": return "primary";
            case "completed": return "success";
            case "cancelled":
            case "refunded": return "info";
            case "refunding": return "danger";
            default: return "info";
        }
    }
}
