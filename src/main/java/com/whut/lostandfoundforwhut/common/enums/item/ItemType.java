package com.whut.lostandfoundforwhut.common.enums.item;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 物品类型
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
public enum ItemType {
    LOST(0, "挂失"),
    FOUND(1, "招领");

    private Integer code;
    private String desc;
}
