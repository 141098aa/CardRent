package com.example.mapper;

import com.example.entity.user.User;

import java.math.BigDecimal;
import java.util.List;

public interface UserMapper {
    List<User> selectAll(String name);

    void deleteById(Integer id);
    // 禁用方法
    void disableById(Integer id);

    // 启用方法
    void enableById(Integer id);

    void insert(User user);

    User selectByUsername(String username);

    void updateById(User user);

    User selectById(Integer id);
    /**
     * 获取所有用户账户余额总和
     */
    BigDecimal getTotalBalance();
}
