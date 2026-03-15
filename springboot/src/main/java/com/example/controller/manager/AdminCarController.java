package com.example.controller.manager;

import com.example.common.Result;
import com.example.entity.car.Car;
import com.example.entity.car.CarBrand;
import com.example.entity.car.CarCategory;
import com.example.service.CarService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/car")
public class AdminCarController {

    @Resource
    private CarService carService;

    // ==================== 车辆管理接口 ====================

    /**
     * 分页查询车辆列表
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer brandId,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) Integer seats,
                             @RequestParam(required = false) String energy,
                             @RequestParam(required = false) String status,
                             @RequestParam(required = false) Integer minPrice,
                             @RequestParam(required = false) Integer maxPrice) {
        PageInfo<Car> pageInfo = carService.selectPage(pageNum, pageSize, keyword, brandId, categoryId,
                seats, energy, status, minPrice, maxPrice);
        return Result.success(pageInfo);
    }

    /**
     * 查询车辆详情
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Car car = carService.selectById(id);
        return Result.success(car);
    }

    /**
     * 添加车辆
     */
    @PostMapping("/add")
    public Result add(@RequestBody Car car) {
        carService.add(car);
        return Result.success();
    }

    /**
     * 修改车辆
     */
    @PutMapping("/update")
    public Result update(@RequestBody Car car) {
        carService.update(car);
        return Result.success();
    }

    /**
     * 删除车辆
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        carService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除车辆
     */
    @DeleteMapping("/batchDelete")
    public Result batchDelete(@RequestBody List<Integer> ids) {
        carService.batchDelete(ids);
        return Result.success();
    }

    /**
     * 更新车辆状态
     */
    @PutMapping("/updateStatus")
    public Result updateStatus(@RequestBody Map<String, Object> params) {
        Integer id = (Integer) params.get("id");
        String status = (String) params.get("status");
        carService.updateStatus(id, status);
        return Result.success();
    }

    // ==================== 品牌管理接口 ====================

    /**
     * 分页查询品牌列表
     */
    @GetMapping("/brand/selectPage")
    public Result selectBrandPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                  @RequestParam(defaultValue = "10") Integer pageSize,
                                  @RequestParam(required = false) String name) {
        PageInfo<CarBrand> pageInfo = carService.selectBrandPage(pageNum, pageSize, name);
        return Result.success(pageInfo);
    }

    /**
     * 查询所有品牌（用于下拉框）
     */
    @GetMapping("/brand/listAll")
    public Result listAllBrands() {
        List<CarBrand> list = carService.listAllBrands();
        return Result.success(list);
    }

    /**
     * 添加品牌
     */
    @PostMapping("/brand/add")
    public Result addBrand(@RequestBody CarBrand brand) {
        carService.addBrand(brand);
        return Result.success();
    }

    /**
     * 修改品牌
     */
    @PutMapping("/brand/update")
    public Result updateBrand(@RequestBody CarBrand brand) {
        carService.updateBrand(brand);
        return Result.success();
    }

    /**
     * 删除品牌
     */
    @DeleteMapping("/brand/delete/{id}")
    public Result deleteBrand(@PathVariable Integer id) {
        carService.deleteBrandById(id);
        return Result.success();
    }

    // ==================== 分类管理接口 ====================

    /**
     * 分页查询分类列表
     */
    @GetMapping("/category/selectPage")
    public Result selectCategoryPage(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String name) {
        PageInfo<CarCategory> pageInfo = carService.selectCategoryPage(pageNum, pageSize, name);
        return Result.success(pageInfo);
    }

    /**
     * 查询所有分类（用于下拉框）
     */
    @GetMapping("/category/listAll")
    public Result listAllCategories() {
        List<CarCategory> list = carService.listAllCategories();
        return Result.success(list);
    }

    /**
     * 添加分类
     */
    @PostMapping("/category/add")
    public Result addCategory(@RequestBody CarCategory category) {
        carService.addCategory(category);
        return Result.success();
    }

    /**
     * 修改分类
     */
    @PutMapping("/category/update")
    public Result updateCategory(@RequestBody CarCategory category) {
        carService.updateCategory(category);
        return Result.success();
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/category/delete/{id}")
    public Result deleteCategory(@PathVariable Integer id) {
        carService.deleteCategoryById(id);
        return Result.success();
    }
}