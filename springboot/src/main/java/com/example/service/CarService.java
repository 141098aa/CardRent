package com.example.service;

import com.example.entity.car.Car;
import com.example.entity.car.CarBrand;
import com.example.entity.car.CarCategory;
import com.example.exception.CustomException;
import com.example.mapper.CarMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@Service
public class CarService {

    // ==================== 管理员端方法 ====================

    @Resource
    private CarMapper carMapper;

    // 车辆管理服务

    public PageInfo<Car> selectPage(Integer pageNum, Integer pageSize, String keyword, Integer brandId,
                                    Integer categoryId, Integer seats, String energy, String status,
                                    Integer minPrice, Integer maxPrice) {
        PageHelper.startPage(pageNum, pageSize);
        List<Car> list = carMapper.selectAll(keyword, brandId, categoryId, seats, energy, status, minPrice, maxPrice);
        return PageInfo.of(list);
    }

    public Car selectById(Integer id) {
        Car car = carMapper.selectById(id);
        if (car == null) {
            throw new CustomException("车辆不存在");
        }
        return car;
    }

    @Transactional
    public void add(Car car) {
        // 参数校验
        if (car.getBrandId() == null) {
            throw new CustomException("请选择品牌");
        }
        if (!StringUtils.hasText(car.getModel())) {
            throw new CustomException("请输入车型");
        }
        if (car.getPrice() == null || car.getPrice().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new CustomException("请输入有效的价格");
        }

        // 设置默认值
        if (car.getStock() == null) {
            car.setStock(0);
        }
        if (car.getRating() == null) {
            car.setRating(java.math.BigDecimal.valueOf(5.0));
        }
        if (car.getReviewCount() == null) {
            car.setReviewCount(0);
        }
        if (!StringUtils.hasText(car.getStatus())) {
            car.setStatus("available");
        }

        carMapper.insert(car);

        // 处理分类关联
        if (car.getCategoryIds() != null && !car.getCategoryIds().isEmpty()) {
            carMapper.insertCategoryRelations(car.getId(), car.getCategoryIds());
        }
    }

    @Transactional
    public void update(Car car) {
        // 检查车辆是否存在
        Car existing = carMapper.selectById(car.getId());
        if (existing == null) {
            throw new CustomException("车辆不存在");
        }

        carMapper.updateById(car);

        // 更新分类关联
        if (car.getCategoryIds() != null) {
            carMapper.deleteCategoryRelations(car.getId());
            if (!car.getCategoryIds().isEmpty()) {
                carMapper.insertCategoryRelations(car.getId(), car.getCategoryIds());
            }
        }
    }

    @Transactional
    public void deleteById(Integer id) {
        carMapper.deleteById(id);
    }

    @Transactional
    public void batchDelete(List<Integer> ids) {
        for (Integer id : ids) {
            carMapper.deleteById(id);
        }
    }

    public void updateStatus(Integer id, String status) {
        Car car = carMapper.selectById(id);
        if (car == null) {
            throw new CustomException("车辆不存在");
        }
        car.setStatus(status);
        carMapper.updateById(car);
    }

    //  品牌管理服务

    public PageInfo<CarBrand> selectBrandPage(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        List<CarBrand> list = carMapper.selectBrandAll(name);
        return PageInfo.of(list);
    }

    public List<CarBrand> listAllBrands() {
        return carMapper.selectBrandAll(null);
    }

    public void addBrand(CarBrand brand) {
        // 检查品牌名是否已存在
        CarBrand existing = carMapper.selectBrandByName(brand.getName());
        if (existing != null) {
            throw new CustomException("品牌名称已存在");
        }
        carMapper.insertBrand(brand);
    }

    public void updateBrand(CarBrand brand) {
        // 检查品牌是否存在
        CarBrand existing = carMapper.selectBrandById(brand.getId());
        if (existing == null) {
            throw new CustomException("品牌不存在");
        }
        // 检查名称是否与其他品牌冲突
        CarBrand nameCheck = carMapper.selectBrandByName(brand.getName());
        if (nameCheck != null && !nameCheck.getId().equals(brand.getId())) {
            throw new CustomException("品牌名称已存在");
        }
        carMapper.updateBrand(brand);
    }

    public void deleteBrandById(Integer id) {
        // 检查是否有车辆使用该品牌
        int count = carMapper.countByBrandId(id);
        if (count > 0) {
            throw new CustomException("该品牌下有车辆，无法删除");
        }
        carMapper.deleteBrandById(id);
    }

    // 分类管理服务

    public PageInfo<CarCategory> selectCategoryPage(Integer pageNum, Integer pageSize, String name) {
        PageHelper.startPage(pageNum, pageSize);
        List<CarCategory> list = carMapper.selectCategoryAll(name);
        return PageInfo.of(list);
    }

    public List<CarCategory> listAllCategories() {
        return carMapper.selectCategoryAll(null);
    }

    public void addCategory(CarCategory category) {
        // 检查分类名是否已存在
        CarCategory existing = carMapper.selectCategoryByName(category.getName());
        if (existing != null) {
            throw new CustomException("分类名称已存在");
        }
        carMapper.insertCategory(category);
    }

    public void updateCategory(CarCategory category) {
        // 检查分类是否存在
        CarCategory existing = carMapper.selectCategoryById(category.getId());
        if (existing == null) {
            throw new CustomException("分类不存在");
        }
        // 检查名称是否与其他分类冲突
        CarCategory nameCheck = carMapper.selectCategoryByName(category.getName());
        if (nameCheck != null && !nameCheck.getId().equals(category.getId())) {
            throw new CustomException("分类名称已存在");
        }
        carMapper.updateCategory(category);
    }

    public void deleteCategoryById(Integer id) {
        // 检查是否有车辆使用该分类
        int count = carMapper.countByCategoryId(id);
        if (count > 0) {
            throw new CustomException("该分类下有车辆，无法删除");
        }
        carMapper.deleteCategoryById(id);
    }

    // ==================== 用户端服务方法 ====================

    /**
     * 用户端分页查询车辆列表
     */
    public PageInfo<Car> selectFrontPage(Integer pageNum, Integer pageSize, String keyword, String brand,
                                         Integer seats, String energy, Integer minPrice, Integer maxPrice,
                                         String sortBy) {
        PageHelper.startPage(pageNum, pageSize);

        // 处理品牌名称转为brandId
        Integer brandId = null;
        if (brand != null && !brand.isEmpty()) {
            CarBrand carBrand = carMapper.selectBrandByName(brand);
            if (carBrand != null) {
                brandId = carBrand.getId();
            }
        }

        List<Car> list = carMapper.selectFrontList(keyword, brandId, seats, energy, minPrice, maxPrice, sortBy);

        // 处理返回数据，只返回前端需要的字段
        for (Car car : list) {
            // 限制features数量，只返回前3个
            if (car.getFeatures() != null && car.getFeatures().size() > 3) {
                car.setFeatures(car.getFeatures().subList(0, 3));
            }
            // 不需要返回configs
            car.setConfigs(null);
        }

        return PageInfo.of(list);
    }

    /**
     * 查询车辆详情（用户端）
     */
    public Car selectFrontDetail(Integer id) {
        Car car = carMapper.selectFrontDetail(id);
        if (car == null) {
            throw new CustomException("车辆不存在");
        }
        return car;
    }

    /**
     * 热门推荐 - 基于评价数量
     * 按评价数量降序，取前limit条
     */
    public List<Car> selectHotRecommend(Integer limit) {
        return carMapper.selectHotRecommend(limit);
    }

    /**
     * 车型推荐 - 基于评分
     * 按评分降序，取前limit条
     */
    public List<Car> selectRatingRecommend(Integer limit) {
        return carMapper.selectRatingRecommend(limit);
    }

    /**
     * 获取所有品牌列表
     */
    public List<Map<String, Object>> selectBrandList() {
        return carMapper.selectBrandList();
    }

    /**
     * 获取所有能源类型
     */
    public List<String> selectEnergyList() {
        return carMapper.selectEnergyList();
    }

    /**
     * 获取所有座位数选项
     */
    public List<Integer> selectSeatsList() {
        return carMapper.selectSeatsList();
    }
}