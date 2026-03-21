package com.example.controller.manager;

import com.example.common.Result;
import com.example.entity.finance.DepositRecord;
import com.example.entity.finance.RefundRequest;
import com.example.entity.finance.TransactionRecord;
import com.example.service.DepositService;
import com.example.service.RefundService;
import com.example.service.TransactionService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/finance")
public class FinanceController {

    @Resource
    private TransactionService transactionService;

    @Resource
    private RefundService refundService;

    @Resource
    private DepositService depositService;

    /**
     * 资金流水列表
     */
    @GetMapping("/transactions")
    public Result getTransactions(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String keyword,
                                  @RequestParam(required = false) String type,
                                  @RequestParam(required = false) String startDate,
                                  @RequestParam(required = false) String endDate) {
        PageInfo<TransactionRecord> pageInfo = transactionService.selectPage(pageNum, pageSize, keyword, type, startDate, endDate);
        return Result.success(pageInfo);
    }

    /**
     * 财务统计（总收入和总支出）
     */
    @GetMapping("/statistics")
    public Result getStatistics(@RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate) {
        Map<String, Object> stats = transactionService.getStatistics(startDate, endDate);
        return Result.success(stats);
    }
    /**
     * 按类型统计收支
     */
    @GetMapping("/statistics/by-type")
    public Result getStatisticsByType(@RequestParam(required = false) String startDate,
                                      @RequestParam(required = false) String endDate) {
        List<Map<String, Object>> stats = transactionService.getStatisticsByType(startDate, endDate);
        return Result.success(stats);
    }

    // 退款管理
    @GetMapping("/refunds")
    public Result getRefunds(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) String startDate,
                             @RequestParam(required = false) String endDate) {
        PageInfo<RefundRequest> pageInfo = refundService.selectPage(pageNum, pageSize, keyword, status, startDate, endDate);
        return Result.success(pageInfo);
    }

    @GetMapping("/refunds/pending/count")
    public Result getPendingRefundCount() {
        int count = refundService.getPendingCount();
        return Result.success(count);
    }

    @PutMapping("/refunds/audit")
    public Result auditRefund(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String action = (String) params.get("action");
        String remark = (String) params.get("remark");
        refundService.auditRefund(id, action, remark);
        return Result.success();
    }

    @PutMapping("/refunds/batch-audit")
    public Result batchAuditRefunds(@RequestBody Map<String, Object> params) {
        List<Integer> ids = (List<Integer>) params.get("ids");
        String action = (String) params.get("action");
        String remark = (String) params.get("remark");
        refundService.batchAudit(ids, action, remark);
        return Result.success();
    }
    // 押金管理
    @GetMapping("/deposits")
    public Result getDeposits(@RequestParam(defaultValue = "1") Integer pageNum,
                              @RequestParam(defaultValue = "10") Integer pageSize,
                              @RequestParam(required = false) String keyword,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String startDate,
                              @RequestParam(required = false) String endDate) {
        PageInfo<DepositRecord> pageInfo = depositService.selectPage(pageNum, pageSize, keyword, status, startDate, endDate);
        return Result.success(pageInfo);
    }

    @GetMapping("/deposits/stats")
    public Result getDepositStats() {
        Map<String, Object> stats = depositService.getDepositStats();
        return Result.success(stats);
    }

    @PostMapping("/deposits/deduct")
    public Result deductDeposit(@RequestBody Map<String, Object> params) {
        Integer depositId = (Integer) params.get("depositId");
        BigDecimal deductAmount = new BigDecimal(params.get("deductAmount").toString());
        String reason = (String) params.get("reason");
        depositService.deductDeposit(depositId, deductAmount, reason);
        return Result.success();
    }
}