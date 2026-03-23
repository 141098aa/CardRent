package com.example.service;

import com.example.entity.user.Message;
import com.example.mapper.MessageMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;

    /**
     * 发送站内信
     */
    public void sendMessage(Integer userId, String title, String content, String type, String link) {
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setLink(link);
        message.setIsRead(false);
        messageMapper.insert(message);
    }

    /**
     * 获取用户所有消息
     */
    public List<Message> getUserMessages(Integer userId) {
        return messageMapper.selectByUserId(userId);
    }

    /**
     * 标记消息为已读
     */
    public void markAsRead(Integer messageId) {
        messageMapper.updateReadStatus(messageId, true, LocalDateTime.now());
    }

    /**
     * 获取未读消息数量
     */
    public int getUnreadCount(Integer userId) {
        return messageMapper.countUnread(userId);
    }

    /**
     * 批量标记已读
     */
    public void markAllAsRead(Integer userId) {
        List<Message> messages = messageMapper.selectByUserId(userId);
        for (Message msg : messages) {
            if (!msg.getIsRead()) {
                messageMapper.updateReadStatus(msg.getId(), true, LocalDateTime.now());
            }
        }
    }
}
