package com.whut.lostandfoundforwhut.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 标签实体，对应 tags 表
 */
@Data
@TableName("tags")
public class Tag {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String name;
}
