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
    public Result submitRealName(@RequestBody Map<String, Object> params) {  // 改为 Map 接收
        UserRealNameAuth auth = new UserRealNameAuth();
        // 从Map中获取userId参数，转换为Integer类型
        auth.setUserId((Integer) params.get("userId"));
        auth.setRealName((String) params.get("realName"));
        auth.setIdNumber((String) params.get("idNumber"));
        auth.setIdFrontImage((String) params.get("idFrontImage"));
        auth.setIdBackImage((String) params.get("idBackImage"));
        auth.setStatus(0); // 默认待审核

        String phone = (String) params.get("phone");  // 获取手机号

        userAuthService.submitRealName(auth, phone);  // 传入 phone 参数
        return Result.success();
    }

    /**
     * 用户提交驾驶证认证
     */
    @PostMapping("/submitDriverLicense")
    public Result submitDriverLicense(@RequestBody Map<String, Object> params) {  // 改为 Map 接收
        UserDriverLicenseAuth auth = new UserDriverLicenseAuth();
        auth.setUserId((Integer) params.get("userId"));
        auth.setDriverName((String) params.get("driverName"));
        auth.setLicenseNumber((String) params.get("licenseNumber"));
        auth.setVehicleType((String) params.get("vehicleType"));

        // 处理日期参数
        if (params.get("validStart") != null) {
            auth.setValidStart(java.time.LocalDate.parse((String) params.get("validStart")));
        }
        if (params.get("validEnd") != null) {
            auth.setValidEnd(java.time.LocalDate.parse((String) params.get("validEnd")));
        }

        auth.setLicenseFrontImage((String) params.get("licenseFrontImage"));
        auth.setLicenseBackImage((String) params.get("licenseBackImage"));
        auth.setStatus(0); // 默认待审核

        String phone = (String) params.get("phone");  // 获取手机号

        userAuthService.submitDriverLicense(auth, phone);  // 传入 phone 参数
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
