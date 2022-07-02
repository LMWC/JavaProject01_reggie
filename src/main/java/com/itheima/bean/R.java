package com.itheima.bean;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@ApiModel("统一响应数据模型类")
@Data
public class R<T> implements Serializable {

    @ApiModelProperty(value = "操作码 0：失败 1：成功",allowableValues ="0,1" )
    private Integer code; //编码：1成功，0和其它数字为失败
    @ApiModelProperty("错误提示信息")
    private String msg; //错误信息
    @ApiModelProperty("真实响应数据")
    private T data; //数据
    @ApiModelProperty("扩展动态数据")
    private Map map = new HashMap(); //动态数据

    public static <T> R<T> success(T object) {
        R<T> r = new R<T>();
        r.data = object;
        r.code = 1;
        return r;
    }

    public static <T> R<T> error(String msg) {
        R r = new R();
        r.msg = msg;
        r.code = 0;
        return r;
    }

    public R<T> add(String key, Object value) {
        this.map.put(key, value);
        return this;
    }

}
