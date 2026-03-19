package com.example.controller.front;

import com.example.common.Result;
import com.example.exception.CustomException;
import com.example.service.UserFavoriteService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

@RestController
@RequestMapping("/user/favorite")
public class UserFavoriteController {

    @Resource
    private UserFavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Result add(@RequestBody Map<String, Object> params) {
        Integer userId = getCurrentUserId();
        Integer targetId = (Integer) params.get("targetId");
        String targetType = (String) params.get("targetType");
        favoriteService.addFavorite(userId, targetId, targetType);
        return Result.success();
    }


    /**
     * 取消收藏
     */
    @DeleteMapping("/remove")
    public Result remove(@RequestParam Integer targetId,
                         @RequestParam String targetType) {
        Integer userId = getCurrentUserId();
        favoriteService.removeFavorite(userId, targetId, targetType);
        return Result.success();
    }

    /**
     * 查询用户的收藏列表
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam String targetType) {
        Integer userId = getCurrentUserId();
        PageInfo<?> pageInfo = favoriteService.getUserFavorites(userId, targetType, pageNum, pageSize);
        return Result.success(pageInfo);
    }

    /**
     * 检查是否已收藏
     */
    @GetMapping("/check")
    public Result check(@RequestParam Integer targetId,
                        @RequestParam String targetType) {
        Integer userId = getCurrentUserId();
        boolean isFavorite = favoriteService.isFavorite(userId, targetId, targetType);
        return Result.success(isFavorite);
    }
    /**
     * 从请求头获取当前用户ID
     */
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
