package com.example.service;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.example.entity.Account;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service//被controller引用

public class UserService {
    @Resource//引用mapper层
    private UserMapper userMapper;

    /**
     * 登录
     */
    public Account login(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("账号或密码错误");
        }
        return dbUser;
    }

    /*分页查询的方法*/
   public PageInfo<User> selectPage(Integer pageNum,Integer pageSize,String name){
       // 分页查询
       PageHelper.startPage(pageNum,pageSize);
       // 执行查询
       List<User> List = userMapper.selectAll(name);
       // 返回分页结果
       return PageInfo.of(List);
   }

    public void deleteById(Integer id) {
       userMapper.deleteById(id);
    }

    public void add(User user) {
        String username = user.getUsername();
        //校验账户是否重复
        User dbUser= userMapper.selectByUsername(username);
        if(dbUser!=null){
            throw new CustomException(("新增失败！账号重复"));
        }
        //密码为空时设置默认密码
       if(StrUtil.isBlank(user.getPassword())){
           user.setPassword("123");
       }
       //默认名字
        if(StrUtil.isBlank(user.getName())){
            user.setName(user.getUsername());
        }
        user.setRole("普通用户");//默认用户角色
        user.setAccount(BigDecimal.ZERO);//默认用户的账户余额
       userMapper.insert(user);
    }

    public void update(User user) {
       //user对象里面必须包含ID，否则无法更新数据
       userMapper.updateById(user);
    }

    public User selectById(Integer id) {
       return userMapper.selectById(id);
    }

    /**
     * 修改密码
     */
    public void updatePassword(Account account) {
        User dbUser = userMapper.selectByUsername(account.getUsername());
        if (ObjectUtil.isNull(dbUser)) {
            throw new CustomException("用户不存在");
        }
        if (!account.getPassword().equals(dbUser.getPassword())) {
            throw new CustomException("原密码错误");
        }
        dbUser.setPassword(account.getNewPassword());
        userMapper.updateById(dbUser);
    }
}
