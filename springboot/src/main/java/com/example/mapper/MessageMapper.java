package com.example.mapper;

import com.example.entity.user.Message;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageMapper {

    void insert(Message message);

    List<Message> selectByUserId(@Param("userId") Integer userId);

    Message selectById(@Param("id") Integer id);

    void updateReadStatus(@Param("id") Integer id,
                          @Param("isRead") Boolean isRead,
                          @Param("readTime") LocalDateTime readTime);

    int countUnread(@Param("userId") Integer userId);
    /**
     * 清空用户所有消息
     */
    void deleteByUserId(@Param("userId") Integer userId);
}
