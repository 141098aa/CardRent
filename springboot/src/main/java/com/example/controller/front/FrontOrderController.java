package com.example.controller.front;

import com.example.common.Result;
import com.example.entity.User;
import com.example.entity.order.Order;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.example.service.OrderService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/front/order")
public class FrontOrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private UserMapper userMapper;

    /**
     * 创建订单
     */
    @PostMapping("/create")
    public Result create(@RequestBody Order order) {
        // 设置初始状态
        order.setStatus("pending_pay");
        //order.setOrderNo(generateOrderNo());

        Order created = orderService.createOrder(order);
        return Result.success(created);
    }

    /**
     * 查询我的订单列表
     */
    @GetMapping("/myOrders")
    public Result myOrders(@RequestParam(defaultValue = "1") Integer pageNum,
                           @RequestParam(defaultValue = "10") Integer pageSize,
                           @RequestParam(required = false) String status) {
        Integer userId = getCurrentUserId();
        PageInfo<Order> pageInfo = orderService.selectUserOrders(pageNum, pageSize, userId, status);
        return Result.success(pageInfo);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        Integer userId = getCurrentUserId();
        Order order = orderService.selectUserOrderById(id, userId);
        return Result.success(order);
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String reason = (String) params.get("reason");
        Integer userId = getCurrentUserId();

        orderService.cancelUserOrder(id, userId, reason);
        return Result.success();
    }

    /**
     * 支付订单
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String paymentMethod = (String) params.get("paymentMethod");
        String paymentPassword = (String) params.get("paymentPassword");
        Integer userId = getCurrentUserId();

        orderService.payOrder(id, userId, paymentMethod, paymentPassword);
        // 返回更新后的用户信息
        User updatedUser = userMapper.selectById(userId);
        return Result.success(updatedUser);
    }

    /**
     * 申请退款
     */
    @PostMapping("/refund")
    public Result refund(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String reason = (String) params.get("reason");
        Integer userId = getCurrentUserId();

        orderService.refundOrder(id, userId, reason);
        return Result.success();
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    public Result stats() {
        Integer userId = getCurrentUserId();
        Map<String, Object> stats = orderService.getUserOrderStats(userId);
        return Result.success(stats);
    }
    /**
     * 用户确认取车
     */
    @PutMapping("/userConfirmPickup")
    public Result userConfirmPickup(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer userId = getCurrentUserId();

        // 验证订单属于当前用户
        Order order = orderService.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限操作");
        }

        orderService.userConfirmPickup(id, userId);
        return Result.success();
    }
    /**
     * 用户申请还车
     */
    @PutMapping("/applyReturn")
    public Result applyReturn(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer userId = getCurrentUserId();

        // 验证订单属于当前用户
        Order order = orderService.selectUserOrderById(id, userId);
        if (order == null) {
            throw new CustomException("订单不存在或无权限操作");
        }

        orderService.applyReturn(id);
        return Result.success();
    }
    /**
     * 价格预览（计算动态价格）
     */
    @GetMapping("/previewPrice")
    public Result previewPrice(@RequestParam Integer carId,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime pickupTime,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime returnTime) {

        Map<String, Object> priceInfo = orderService.calculatePrice(carId, pickupTime.toString(), returnTime.toString());
        return Result.success(priceInfo);
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

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis();
    }
}