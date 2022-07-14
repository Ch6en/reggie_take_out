package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.chen.reggie.common.BaseContext;
import com.chen.reggie.common.R;
import com.chen.reggie.dto.OrdersDto;
import com.chen.reggie.entity.OrderDetail;
import com.chen.reggie.entity.Orders;
import com.chen.reggie.service.OrderDetailService;
import com.chen.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/order")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderDetailService orderDetailService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("用户下单：{}", orders);
        orderService.submit(orders);
        return R.success("下单成功");

    }


    //抽离的一个方法，通过订单id查询订单明细，得到一个订单明细的集合
    //这里抽离出来是为了避免在stream中遍历的时候直接使用构造条件来查询导致eq叠加，从而导致后面查询的数据都是null
    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId){
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        return orderDetailList;
    }

    /**
     * 查看订单
     * @param page
     * @param pageSize
     * @return
     */
//    方法不可行
//    @GetMapping("/userPage")
//    public R<Page> userPage(int page, int pageSize) {
//        log.info("用户查看订单");
//        Page<Orders> pageInfo = new Page<>(page, pageSize);
//        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
//        queryWrapper.orderByDesc(Orders::getOrderTime);
//
//        orderService.page(pageInfo, queryWrapper);
//
//        return R.success(pageInfo);
//    }

    @GetMapping("/userPage")
    public R<Page> userPage(int page, int pageSize){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> pageDto = new Page<>(page,pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        //这里是直接把当前用户分页的全部结果查询出来，要添加用户id作为查询条件，否则会出现用户可以查询到其他用户的订单情况
        //添加排序条件，根据更新时间降序排列
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);

        //通过OrderId查询对应的OrderDetail
        LambdaQueryWrapper<OrderDetail> queryWrapper2 = new LambdaQueryWrapper<>();

        //对OrderDto进行需要的属性赋值
        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> orderDtoList = records.stream().map((item) ->{
            OrdersDto orderDto = new OrdersDto();
            //此时的orderDto对象里面orderDetails属性还是空 下面准备为它赋值
            Long orderId = item.getId();//获取订单id
            List<OrderDetail> orderDetailList = this.getOrderDetailListByOrderId(orderId);
            BeanUtils.copyProperties(item,orderDto);
            //对orderDto进行OrderDetails属性的赋值
            orderDto.setOrderDetails(orderDetailList);
            return orderDto;
        }).collect(Collectors.toList());
        //使用dto的分页有点难度.....需要重点掌握
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        pageDto.setRecords(orderDtoList);
        return R.success(pageDto);
    }

    /**
     * 分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number,
                        @DateTimeFormat(pattern = "yyyy-mm-dd HH:mm:ss") Date beginTime,
                        @DateTimeFormat(pattern = "yyyy-mm-dd HH:mm:ss") Date endTime) {

        // 构建订单分页对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);

        // 构造条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();

        // 模糊查询根据订单号或者时间间隔查询
        // SELECT * FROM Order where number like ? or order_time between ? and ?
        queryWrapper.like(Strings.isNotEmpty(number), Orders::getNumber, number);

        if (beginTime != null) {
            queryWrapper.between(Orders::getOrderTime, beginTime, endTime);
        }

        //根据时间降序
        queryWrapper.orderByDesc(Orders::getOrderTime);

        // 封装为分页对象
        orderService.page(pageInfo, queryWrapper);

        return R.success(pageInfo);
    }

    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    public R<String> order(@RequestBody Orders orders) {
        orderService.updateById(orders);
        return R.success("订单状态修改成功");
    }

}
