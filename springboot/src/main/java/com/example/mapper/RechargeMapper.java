package com.example.mapper;

import com.example.entity.finance.RechargeRecord;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RechargeMapper {

    void insert(RechargeRecord record);

    void update(RechargeRecord record);

    RechargeRecord selectByRechargeNo(@Param("rechargeNo") String rechargeNo);

    List<RechargeRecord> selectByUserId(@Param("userId") Integer userId,
                                        @Param("status") String status);

    RechargeRecord selectById(@Param("id") Integer id);
    /**
     * 查询过期的待支付充值记录
     */
    List<RechargeRecord> selectExpiredPending(@Param("expireTime") LocalDateTime expireTime);
}