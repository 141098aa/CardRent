package com.example.service;

import com.example.entity.content.Banner;
import com.example.exception.CustomException;
import com.example.mapper.BannerMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BannerService {

    @Resource
    private BannerMapper bannerMapper;

    public PageInfo<Banner> selectPage(Integer pageNum, Integer pageSize, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        List<Banner> list = bannerMapper.selectAll(keyword);
        return PageInfo.of(list);
    }

    public Banner selectById(Integer id) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new CustomException("轮播图不存在");
        }
        return banner;
    }

    @Transactional
    public void add(Banner banner) {
        if (!StringUtils.hasText(banner.getTitle())) {
            throw new CustomException("请输入标题");
        }
        if (!StringUtils.hasText(banner.getImage())) {
            throw new CustomException("请上传图片");
        }
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        bannerMapper.insert(banner);
    }

    @Transactional
    public void update(Banner banner) {
        Banner existing = bannerMapper.selectById(banner.getId());
        if (existing == null) {
            throw new CustomException("轮播图不存在");
        }
        bannerMapper.update(banner);
    }

    @Transactional
    public void deleteById(Integer id) {
        bannerMapper.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<Integer> ids) {
        bannerMapper.batchDelete(ids);
    }

    @Transactional
    public void updateStatus(Integer id, Integer status) {
        Banner banner = bannerMapper.selectById(id);
        if (banner == null) {
            throw new CustomException("轮播图不存在");
        }
        bannerMapper.updateStatus(id, status);
    }

    public List<Banner> getEnabledBanners() {
        return bannerMapper.selectEnabled();
    }
}