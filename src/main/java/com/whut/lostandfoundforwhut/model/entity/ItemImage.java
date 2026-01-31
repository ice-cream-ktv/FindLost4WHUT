package com.whut.lostandfoundforwhut.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 物品-图片关联实体，对应 item_images 表
 */
@Data
@TableName("item_images")
public class ItemImage {
    @TableField("item_id")
    private Long itemId;
    @TableField("image_id")
    private Long imageId;
}
