package com.whut.lostandfoundforwhut.model.dto;

import lombok.Data;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 分页查询参数 DTO
 */
@Data
public class PageQueryDTO {
    /** 当前页（从 1 开始） */
    private Integer pageNo = 1;
    /** 每页大小 */
    private Integer pageSize = 10;
}
