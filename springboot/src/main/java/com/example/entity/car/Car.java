package com.example.entity.car;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Car {
    private Integer id;
    private Integer brandId;
    private String brandName;      // 品牌名称（用于列表显示）
    private String model;
    private Integer seats;
    private String gear;
    private String energy;
    private Integer year;
    private BigDecimal price;
    private Integer stock;
    private String status;          // available:可租, rented:已租, maintenance:维修
    private BigDecimal rating;
    private Integer reviewCount;
    private String tag;
    private String mileage;
    private String displacement;
    private String description;
    private String image;
    private List<String> features;  // JSON格式存储
    private List<Object> configs;    // JSON格式存储
    private String pickupLocation;
    private String returnLocation;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private BigDecimal insurancePrice;  // 基础保险费/天
    private BigDecimal deposit;         // 押金金额

    // 分类ID列表（用于前端提交）
    private List<Integer> categoryIds;

}
