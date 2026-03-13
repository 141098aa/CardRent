package com.example.mapper;

import com.example.entity.UserRealNameAuth;
import com.example.entity.UserDriverLicenseAuth;
import com.example.entity.dto.AuthAuditDTO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
public interface UserAuthMapper {
    //  实名认证
    List<UserRealNameAuth> selectRealNamePage(@Param("status") Integer status);

    UserRealNameAuth selectRealNameById(@Param("id") Integer id);

    UserRealNameAuth selectRealNameByUserId(@Param("userId") Integer userId);

    void updateRealNameStatus(@Param("id") Integer id,
                              @Param("status") Integer status,
                              @Param("auditRemark") String auditRemark,
                              @Param("auditTime") LocalDateTime auditTime);

    // 驾驶证认证
    List<UserDriverLicenseAuth> selectDriverLicensePage(@Param("status") Integer status);

    UserDriverLicenseAuth selectDriverLicenseById(@Param("id") Integer id);

    UserDriverLicenseAuth selectDriverLicenseByUserId(@Param("userId") Integer userId);

    void updateDriverLicenseStatus(@Param("id") Integer id,
                                   @Param("status") Integer status,
                                   @Param("auditRemark") String auditRemark,
                                   @Param("auditTime") LocalDateTime auditTime);

    //  统一查询（用于列表页）
    List<AuthAuditDTO> selectAllAuthPage(@Param("status") Integer status,
                                         @Param("authType") String authType);

    // 更新用户表中的认证状态
    void updateUserAuthStatus(@Param("userId") Integer userId,
                              @Param("authType") String authType,
                              @Param("status") Integer status);

    // ========== 用户端方法 ==========

    void insertRealNameAuth(UserRealNameAuth auth);
    void updateRealNameAuth(UserRealNameAuth auth);

    void insertDriverLicenseAuth(UserDriverLicenseAuth auth);
    void updateDriverLicenseAuth(UserDriverLicenseAuth auth);
}
