package com.whut.lostandfoundforwhut.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemFilter {

    // 分页参数
    private Integer pageNo = 1;
    private Integer pageSize = 10;

    // 原有过滤参数
    private Integer type;
    private Integer status;

    // 标签列表
    private List<String> tags;

    // 时间段筛选
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}