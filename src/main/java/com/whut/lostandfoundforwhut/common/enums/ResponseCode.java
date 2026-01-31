package com.whut.lostandfoundforwhut.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 统一响应码定义
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ResponseCode {

    SUCCESS("0000", "成功"),
    UN_ERROR("0001", "未知失败"),
    ILLEGAL_PARAMETER("0002", "非法参数"),
    NOT_LOGIN("0003", "未登录或登录已过期"),
    NO_PERMISSION("0004", "无权限"),
    RESOURCE_NOT_FOUND("0005", "资源不存在"),
    DUPLICATE_OPERATION("0006", "重复操作"),

    USER_NOT_FOUND("USR_001", "用户不存在"),
    USER_STATUS_INVALID("USR_002", "用户状态异常"),

    ITEM_NOT_FOUND("ITEM_001", "物品不存在"),
    ITEM_STATUS_INVALID("ITEM_002", "物品状态异常"),
    ITEM_CLAIM_CONFLICT("ITEM_003", "物品认领冲突"),
    ;

    private String code;
    private String info;
}
