package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;


@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;

    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;

        // 先尝试管理员登录
        try {
            ac = adminService.login(account);
        } catch (CustomException e) {
            // 管理员不存在或密码错误，继续尝试普通用户
        }

        // 管理员登录失败，尝试普通用户登录
        if (ac == null) {
            try {
                ac = userService.login(account);
            } catch (CustomException e) {
                // 用户也不存在或密码错误
            }
        }

        if (ac == null) {
            return Result.error("登录失败，用户不存在");
        }
        return Result.success(ac);
    }
   /* @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        Account ac = null;
        if ("管理员".equals(account.getRole())) {
            ac = adminService.login(account);
        }
        if ("普通用户".equals(account.getRole())) {
            ac = userService.login(account);
        }
        if(ac==null){
            return Result.error("登录失败，用户不存在");
        }
        return Result.success(ac);
    }*/

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if(!user.getPassword().equals((user.getNewPassword()))){
            return Result.error("两次输入的密码不一致");
        }
        userService.add(user);
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if ("管理员".equals(account.getRole())) {
            adminService.updatePassword(account);
        }
        if ("普通用户".equals(account.getRole())) {
            userService.updatePassword(account);
        }
        return Result.success();
    }

}
