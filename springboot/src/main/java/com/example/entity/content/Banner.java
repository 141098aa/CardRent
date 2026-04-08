package com.example.entity.content;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Banner {
    private Integer id;
    private String title;
    private String description;
    private String image;
    private String keyword;
    private String link;
    private Integer sortOrder;
    private Integer status;  // 0禁用 1启用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}