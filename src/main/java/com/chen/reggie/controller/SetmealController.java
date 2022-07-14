package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.reggie.common.R;
import com.chen.reggie.dto.SetmealDto;
import com.chen.reggie.entity.Category;
import com.chen.reggie.entity.Setmeal;
import com.chen.reggie.service.CategoryService;
import com.chen.reggie.service.SetmealDishService;
import com.chen.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("套餐信息:{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name) {
        //构造分页构造器对象
        Page<Setmeal> pageInfo = new Page<>(page, pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null, Setmeal::getName, name);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行分页查询
        setmealService.page(pageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(pageInfo, dtoPage, "records");

        List<Setmeal> records = pageInfo.getRecords();

        List<SetmealDto> list = records.stream().map((item) -> {

            SetmealDto setmealDto = new SetmealDto();

            //对象拷贝
            BeanUtils.copyProperties(item, setmealDto);

            //分类id
            Long categoryId = item.getCategoryId();

            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if (category != null) {
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }

            return setmealDto;

        }).collect(Collectors.toList());

            dtoPage.setRecords(list);

        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("套餐id：{}", ids);
        setmealService.removeWithDish(ids);
        return R.success("套餐删除成功");
    }

    /**
     * 根据条件查询套餐数据
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        Long categoryId = setmeal.getCategoryId();
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(categoryId != null, Setmeal::getCategoryId, categoryId);
        queryWrapper.eq(setmeal.getStatus() != 0, Setmeal::getStatus, setmeal.getStatus());
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        List<Setmeal> list = setmealService.list(queryWrapper);

        return R.success(list);
    }

    /**
     * 根据id查询套餐数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getById(@PathVariable Long id) {
        log.info("根据id查询套餐：{}", id);
        SetmealDto setmealDto = setmealService.getByIdWithDish(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐信息
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        setmealService.updateWithSetmeal(setmealDto);
        return R.success("修改成功");
    }

    /**
     * 停售、启售套餐
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> status(@PathVariable Integer status, Long[] ids) {
        for (int i = 0; i < ids.length; i++) {
            Setmeal setmeal = setmealService.getById(ids[i]);
            setmeal.setStatus(status);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功");
    }

}
