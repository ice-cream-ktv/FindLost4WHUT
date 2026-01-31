package com.whut.lostandfoundforwhut.common.enums.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 用户状态
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum UserStatus {
    NORMAL(0, "正常"),
    BANNED(1, "封禁"),
    DEACTIVATED(2, "注销");

    private Integer code;
    private String desc;
}
