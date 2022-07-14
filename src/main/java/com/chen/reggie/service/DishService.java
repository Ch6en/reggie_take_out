package com.chen.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.chen.reggie.dto.DishDto;
import com.chen.reggie.entity.Dish;

import java.util.List;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void dishWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息和对应的口味信息
    public void updateWithFlavor(DishDto dishDto);

    //根据id删除菜品
    public void removeWithDish(List<Long> ids);


}
