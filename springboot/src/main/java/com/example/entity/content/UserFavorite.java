package com.example.entity.content;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserFavorite {
    private Integer id;
    private Integer userId;
    private Integer targetId;
    private String targetType;  // "car" 或 "news"
    private LocalDateTime createdTime;
}
