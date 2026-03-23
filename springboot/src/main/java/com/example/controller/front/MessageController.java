package com.example.controller.front;

import com.example.common.Result;
import com.example.entity.user.Message;
import com.example.exception.CustomException;
import com.example.service.MessageService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@RestController
@RequestMapping("/front/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /**
     * 获取用户消息列表
     */
    @GetMapping("/list")
    public Result getMessages() {
        Integer userId = getCurrentUserId();
        List<Message> messages = messageService.getUserMessages(userId);
        return Result.success(messages);
    }

    /**
     * 获取未读消息数量
     */
    @GetMapping("/unread/count")
    public Result getUnreadCount() {
        Integer userId = getCurrentUserId();
        int count = messageService.getUnreadCount(userId);
        return Result.success(count);
    }

    /**
     * 标记消息为已读
     */
    @PutMapping("/read/{id}")
    public Result markAsRead(@PathVariable Integer id) {
        messageService.markAsRead(id);
        return Result.success();
    }

    /**
     * 全部标记已读
     */
    @PutMapping("/read/all")
    public Result markAllAsRead() {
        Integer userId = getCurrentUserId();
        messageService.markAllAsRead(userId);
        return Result.success();
    }
    private Integer getCurrentUserId() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String userIdStr = request.getHeader("X-User-Id");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new CustomException("未登录");
        }
        try {
            return Integer.parseInt(userIdStr);
        } catch (NumberFormatException e) {
            throw new CustomException("无效的用户ID");
        }
    }
}
