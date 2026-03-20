package com.example.mapper;

import com.example.entity.finance.PaymentPasswordReset;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PaymentPasswordResetMapper {

    void insert(PaymentPasswordReset reset);

    PaymentPasswordReset selectByUserIdAndCode(@Param("userId") Integer userId,
                                               @Param("resetCode") String resetCode);

    void update(PaymentPasswordReset reset);

    void deleteExpired();

    PaymentPasswordReset selectLatestByUserId(@Param("userId") Integer userId);
}
