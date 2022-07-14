package com.chen.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.chen.reggie.common.CustomException;
import com.chen.reggie.dto.DishDto;
import com.chen.reggie.entity.Dish;
import com.chen.reggie.entity.DishFlavor;
import com.chen.reggie.mapper.DishMapper;
import com.chen.reggie.service.DishFlavorService;
import com.chen.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDto
     */
    @Override
    @Transactional
    public void dishWithFlavor(DishDto dishDto) {

        //保存菜品的基本信息到菜品表dish中
        this.save(dishDto);

        //菜品id
        Long dishDtoId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDtoId);
            return item;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);

    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @Override
    public DishDto getByIdWithFlavor(Long id) {
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        BeanUtils.copyProperties(dish, dishDto);

        //查询当前菜品对应的口味信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    /**
     *修改菜品
     * @param dishDto
     */
    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //更新dish表基本信息
        this.updateById(dishDto);

        //清理当前菜品对应的口味数据——dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId, dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //添加当前提交过来的口味数据——dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();

        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id删除菜品
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Dish::getId, ids);
        queryWrapper.eq(Dish::getStatus, 1);

        //查询启售的菜品的总条数
        int count = this.count(queryWrapper);
        if (count > 0) {
            throw new CustomException("菜品正在出售，不能删除");
        }

        //先删除对应的口味表数据
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId, ids);

        dishFlavorService.remove(lambdaQueryWrapper);

        //最后删除菜品
        this.removeByIds(ids);

    }


}
