package com.example.controller.manager;

import com.example.common.Result;
import com.example.entity.content.Banner;
import com.example.service.BannerService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/manager/banner")
public class BannerController {

    @Resource
    private BannerService bannerService;

    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize,
                       @RequestParam(required = false) String keyword) {
        PageInfo<Banner> pageInfo = bannerService.selectPage(pageNum, pageSize, keyword);
        return Result.success(pageInfo);
    }

    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Integer id) {
        Banner banner = bannerService.selectById(id);
        return Result.success(banner);
    }

    @PostMapping("/add")
    public Result add(@RequestBody Banner banner) {
        bannerService.add(banner);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody Banner banner) {
        bannerService.update(banner);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        bannerService.deleteById(id);
        return Result.success();
    }

    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        bannerService.batchDelete(ids);
        return Result.success();
    }

    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        Integer status = (Integer) params.get("status");
        bannerService.updateStatus(id, status);
        return Result.success();
    }
}