package com.example.controller;

import com.example.common.Result;
import com.example.entity.UserDriverLicenseAuth;
import com.example.entity.UserRealNameAuth;
import com.example.entity.dto.AuthAuditDTO;
import com.example.service.UserAuthService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/userAuth")
public class UserAuthController {

    @Resource
    private UserAuthService userAuthService;

    /**
     * 分页查询所有认证
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) Integer status,
                             @RequestParam(required = false) String authType) {
        PageInfo<AuthAuditDTO> pageInfo = userAuthService.selectPage(pageNum, pageSize, status, authType);
        return Result.success(pageInfo);
    }

    /**
     * 审核实名认证
     */
    @PutMapping("/auditRealName")
    public Result auditRealName(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer status = (Integer) params.get("status");
        String remark = (String) params.get("remark");

        userAuthService.auditRealName(id, status, remark);
        return Result.success();
    }

    /**
     * 审核驾驶证认证
     */
    @PutMapping("/auditDriverLicense")
    public Result auditDriverLicense(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer status = (Integer) params.get("status");
        String remark = (String) params.get("remark");

        userAuthService.auditDriverLicense(id, status, remark);
        return Result.success();
    }

    /**
     * 批量审核
     */
    @PutMapping("/batchAudit")
    public Result batchAudit(@RequestBody Map<String, Object> params) {
        List<Integer> ids = (List<Integer>) params.get("ids");
        String authType = (String) params.get("authType");
        Integer status = (Integer) params.get("status");
        String remark = (String) params.get("remark");

        userAuthService.batchAudit(ids, authType, status, remark);
        return Result.success();
    }

    /**
     * 获取认证统计信息
     */
    @GetMapping("/stats")
    public Result getStats() {
        // 这里可以查询各状态的数量
        return Result.success();
    }

    // ========== 用户端接口 ==========

    /**
     * 用户提交实名认证
     */
    @PostMapping("/submitRealName")
    public Result submitRealName(@RequestBody UserRealNameAuth auth) {
        auth.setStatus(0); // 默认待审核
        userAuthService.submitRealName(auth);
        return Result.success();
    }

    /**
     * 用户提交驾驶证认证
     */
    @PostMapping("/submitDriverLicense")
    public Result submitDriverLicense(@RequestBody UserDriverLicenseAuth auth) {
        auth.setStatus(0); // 默认待审核
        userAuthService.submitDriverLicense(auth);
        return Result.success();
    }

    /**
     * 查询用户的实名认证状态
     */
    @GetMapping("/realName/status/{userId}")
    public Result getRealNameStatus(@PathVariable Integer userId) {
        UserRealNameAuth auth = userAuthService.getRealNameByUserId(userId);
        return Result.success(auth);
    }

    /**
     * 查询用户的驾驶证认证状态
     */
    @GetMapping("/driverLicense/status/{userId}")
    public Result getDriverLicenseStatus(@PathVariable Integer userId) {
        UserDriverLicenseAuth auth = userAuthService.getDriverLicenseByUserId(userId);
        return Result.success(auth);
    }
}
