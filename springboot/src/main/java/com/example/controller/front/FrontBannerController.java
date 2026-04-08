package com.example.controller.front;

import com.example.common.Result;
import com.example.entity.content.Banner;
import com.example.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/front/banner")
public class FrontBannerController {

    @Resource
    private BannerService bannerService;

    /**
     * 获取所有启用的轮播图（供首页展示）
     */
    @GetMapping("/list")
    public Result list() {
        List<Banner> banners = bannerService.getEnabledBanners();
        return Result.success(banners);
    }
}