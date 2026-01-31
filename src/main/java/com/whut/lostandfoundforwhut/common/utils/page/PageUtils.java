package com.whut.lostandfoundforwhut.common.utils.page;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.whut.lostandfoundforwhut.model.vo.PageResultVO;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 分页工具类
 */
public class PageUtils {
    private PageUtils() {
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 将 MyBatis-Plus 的 IPage 转换为统一 PageResult
     *
     * 前端需要自行计算：
     * 1) 总页数 totalPages = ceil(total / pageSize)
     * 2) 是否有下一页 hasNext = pageNo < totalPages
     * 3) 是否有上一页 hasPrevious = pageNo > 1
     * 4) 排序字段 orderBy 由后端控制，前端无需关注
     *
     * @param page MP 分页对象
     * @return PageResultVO 统一分页结果
     * @param <T> 数据类型
     */
    public static <T> PageResultVO<T> toPageResult(IPage<T> page) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setPageNo(page.getCurrent());
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotal());
        result.setRecords(page.getRecords());
        return result;
    }
}
