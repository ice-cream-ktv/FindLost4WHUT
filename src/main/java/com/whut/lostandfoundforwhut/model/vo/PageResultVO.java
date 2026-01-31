package com.whut.lostandfoundforwhut.model.vo;

import lombok.Data;

import java.util.List;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 分页结果 VO
 */
@Data
public class PageResultVO<T> {
    /** 当前页 */
    private long pageNo;
    /** 每页大小 */
    private long pageSize;
    /** 总条数 */
    private long total;
    /** 数据列表 */
    private List<T> records;
}
