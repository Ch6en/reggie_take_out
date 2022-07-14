package com.chen.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.chen.reggie.common.BaseContext;
import com.chen.reggie.common.R;
import com.chen.reggie.entity.AddressBook;
import com.chen.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;
    
    /**
     * 新增收货地址
     * @param addressBook
     * @return
     */
    @PostMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info(addressBook.toString());
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    /**
     * 设置默认地址
     */
    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook) {
        log.info("addressBook:{}", addressBook);
        LambdaUpdateWrapper<AddressBook> updateWrapper = new LambdaUpdateWrapper<>();
        Long currentId = BaseContext.getCurrentId();

        updateWrapper.eq(AddressBook::getUserId, currentId);
        updateWrapper.set(AddressBook::getIsDefault, 0);
        //UPDATE address_book SET is_default=? WHERE (user_id = ?)
        addressBookService.update(updateWrapper);

        addressBook.setIsDefault(1);
        //UPDATE address_book SET is_default=?, update_time=?, update_user=? WHERE id=?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    /**
     * 根据id查询地址
     */
    @GetMapping("/{id}")
    public R get(@PathVariable Long id) {
        AddressBook addressBook = addressBookService.getById(id);
        if (addressBook != null) {
            return R.success(addressBook);
        }else {
            return R.error("没有找到该对象");
        }
    }

    /**
     * 查询默认地址
     */
    @GetMapping("/default")
    public R<AddressBook> getDefault() {
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        Long currentId = BaseContext.getCurrentId();
        queryWrapper.eq(AddressBook::getUserId, currentId);
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        //SQL:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if (addressBook != null) {
            return R.success(addressBook);
        }else {
            return R.error("没有找到该对象");
        }
    }


    /**
     *查询指定用户的全部地址
     * @param addressBook
     * @return
     */
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper();
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);

        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook) {
        log.info("修改地址：{}", addressBook);
        addressBookService.updateById(addressBook);
        return R.success("修改成功");
    }

    /**
     * 根据id删除地址
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long ids) {
        log.info("id：", ids);
        addressBookService.removeById(ids);
        return R.success("删除成功");
    }


}
