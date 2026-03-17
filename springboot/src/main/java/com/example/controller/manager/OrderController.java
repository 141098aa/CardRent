package com.example.controller.manager;

import com.example.common.Result;
import com.example.entity.order.Order;
import com.example.service.OrderService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /**
     * 分页查询订单
     * @param statusGroup 状态分组: all/pending/active/completed
     */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String statusGroup,
                       @RequestParam(required = false) String startDate,
                       @RequestParam(required = false) String endDate) {
        PageInfo<Order> pageInfo = orderService.selectPage(pageNum, pageSize, keyword, statusGroup, startDate, endDate);
        return Result.success(pageInfo);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        Order order = orderService.selectById(id);
        return Result.success(order);
    }

    /**
     * 审核订单（待审核 -> 待取车/已取消）
     */
    @PutMapping("/audit")
    public Result audit(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String action = (String) params.get("action"); // approve/reject
        String remark = (String) params.get("remark");
        orderService.auditOrder(id, action, remark);
        return Result.success();
    }

    /**
     * 确认取车
     */
    @PutMapping("/confirmPickup")
    public Result confirmPickup(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        orderService.confirmPickup(id);
        return Result.success();
    }

    /**
     * 确认还车
     */
    @PutMapping("/confirmReturn")
    public Result confirmReturn(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        orderService.confirmReturn(id);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    public Result cancel(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String reason = (String) params.get("reason");
        orderService.cancelOrder(id, reason);
        return Result.success();
    }

    /**
     * 获取订单统计
     */
    @GetMapping("/stats")
    public Result stats() {
        Map<String, Object> stats = orderService.getOrderStats();
        return Result.success(stats);
    }
}
