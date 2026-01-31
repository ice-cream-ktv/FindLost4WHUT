package com.whut.lostandfoundforwhut.common.result;

import com.whut.lostandfoundforwhut.common.enums.ResponseCode;
import lombok.Data;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 统一返回体
 */
@Data
public class Result<T> {
    private String code;
    private String info;
    private T data;

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 成功返回
     * @param data 数据
     * @return Result 成功结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = ResponseCode.SUCCESS.getCode();
        r.info = ResponseCode.SUCCESS.getInfo();
        r.data = data;
        return r;
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 失败返回（枚举码）
     * @param code 错误码枚举
     * @return Result 失败结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> fail(ResponseCode code) {
        Result<T> r = new Result<>();
        r.code = code.getCode();
        r.info = code.getInfo();
        return r;
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 失败返回（自定义码）
     * @param code 错误码
     * @param info 错误信息
     * @return Result 失败结果
     * @param <T> 数据类型
     */
    public static <T> Result<T> fail(String code, String info) {
        Result<T> r = new Result<>();
        r.code = code;
        r.info = info;
        return r;
    }
}
