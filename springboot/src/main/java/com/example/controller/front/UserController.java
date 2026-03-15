package com.example.controller.front;

import com.example.common.Result;
import com.example.entity.Admin;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController //表示这个类是接口会返回json数据
@RequestMapping("/user")//接口统一的前缀
//controller会调用service
public class UserController {
    @Resource
    private UserService userService;
    /*
    * pageNum 当前页码
    * pageSize 每页展示的个数
    * return 分页数据
    * 接口请求方式：http://localhost:9090/user/selectPage?pageNum=1&pageSize=10
    * */
    /*
    * 分页和查询数据*/
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String name)
    {
        PageInfo<User> pageInfo= userService.selectPage(pageNum,pageSize,name);
        return Result.success(pageInfo);
    }
    /*
    * 删除数据
    * 接口路径：/user/delete/1
    * */
   @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        userService.deleteById(id);
        return Result.success();
   }
   /*
   新增数据
   * */
    @PostMapping("/add")
    public Result add(@RequestBody User user){
        userService.add(user);
        return Result.success();
    }
    /*
    更新数据
* */
    @PutMapping("/update")
    public Result update(@RequestBody User user){
        userService.update(user);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        User user = userService.selectById(id);
        return Result.success(user);
    }
}
