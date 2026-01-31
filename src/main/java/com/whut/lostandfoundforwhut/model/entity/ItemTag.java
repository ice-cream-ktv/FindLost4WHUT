package com.whut.lostandfoundforwhut.model.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 物品-标签关联实体，对应 item_tags 表
 */
@Data
@TableName("item_tags")
public class ItemTag {
    @TableField("item_id")
    private Long itemId;
    @TableField("tag_id")
    private Long tagId;
}
