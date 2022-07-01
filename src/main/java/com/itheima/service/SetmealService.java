package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.bean.Setmeal;
import com.itheima.dto.SetmealDto;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    boolean saveWithDish(SetmealDto setmealDto);

    boolean removeWithDish(List<Long> ids);

    boolean updateWithStatus(Integer sts, List<Long> ids);
}
