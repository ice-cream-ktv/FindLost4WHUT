package com.whut.lostandfoundforwhut.common.enums.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 物品状态
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ItemStatus {
    ACTIVE(0, "有效"),
    CLOSED(1, "结束");

    private Integer code;
    private String desc;
}
