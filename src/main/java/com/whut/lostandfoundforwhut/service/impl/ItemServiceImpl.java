package com.whut.lostandfoundforwhut.service.impl;

import com.alibaba.dashscope.embeddings.TextEmbedding;
import com.alibaba.dashscope.embeddings.TextEmbeddingParam;
import com.alibaba.dashscope.embeddings.TextEmbeddingResult;
import com.alibaba.dashscope.embeddings.TextEmbeddingResultItem;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.whut.lostandfoundforwhut.common.enums.ResponseCode;
import com.whut.lostandfoundforwhut.common.enums.item.ItemStatus;
import com.whut.lostandfoundforwhut.common.exception.AppException;
import com.whut.lostandfoundforwhut.mapper.ItemMapper;
import com.whut.lostandfoundforwhut.mapper.ItemTagMapper;
import com.whut.lostandfoundforwhut.mapper.TagMapper;
import com.whut.lostandfoundforwhut.mapper.UserMapper;
import com.whut.lostandfoundforwhut.model.dto.ItemDTO;
import com.whut.lostandfoundforwhut.model.dto.ItemFilter;
import com.whut.lostandfoundforwhut.model.entity.Item;
import com.whut.lostandfoundforwhut.model.entity.ItemTag;
import com.whut.lostandfoundforwhut.model.entity.Tag;
import com.whut.lostandfoundforwhut.model.entity.User;
import com.whut.lostandfoundforwhut.model.vo.PageResultVO;
import com.whut.lostandfoundforwhut.service.IItemService;
import com.whut.lostandfoundforwhut.common.utils.page.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Qoder
 * @date 2026/01/31
 * @description 物品服务实现类
 */
@Service
@RequiredArgsConstructor
public class ItemServiceImpl extends ServiceImpl<ItemMapper, Item> implements IItemService {

    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final TagMapper tagMapper;
    private final ItemTagMapper itemTagMapper;

    @Override
    @Transactional
    public Item addItem(ItemDTO itemDTO, Long userId) {
        System.out.println("开始添加物品，用户ID：" + userId);
        // 验证用户是否存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new AppException(ResponseCode.USER_NOT_FOUND.getCode(), ResponseCode.USER_NOT_FOUND.getInfo());
        }

        Item item = new Item();
        item.setUserId(userId);
        item.setType(itemDTO.getType());
        item.setEventTime(itemDTO.getEventTime());
        item.setEventPlace(itemDTO.getEventPlace());
        // 设置默认状态为有效
        item.setStatus(ItemStatus.ACTIVE.getCode());
        item.setDescription(itemDTO.getDescription());

        // 保存到数据库
        int rowsAffected = itemMapper.insert(item);
        System.out.println("数据库影响行数：" + rowsAffected);
        System.out.println("物品创建成功：" + item);

        return item;
    }

    @Override
    public Item updateItem(Long itemId, ItemDTO itemDTO, Long userId) {
        // 查询物品是否存在
        Item existingItem = itemMapper.selectById(itemId);
        if (existingItem == null) {
            throw new AppException(ResponseCode.ITEM_NOT_FOUND.getCode(), ResponseCode.ITEM_NOT_FOUND.getInfo());
        }
        if (!existingItem.getUserId().equals(userId)) {
            throw new AppException(ResponseCode.NO_PERMISSION.getCode(), ResponseCode.NO_PERMISSION.getInfo());
        }
        if (ItemStatus.CLOSED.getCode().equals(existingItem.getStatus())) {
            throw new AppException(ResponseCode.ITEM_STATUS_INVALID.getCode(),
                    ResponseCode.ITEM_STATUS_INVALID.getInfo());
        }

        // 更新物品信息
        if (itemDTO.getType() != null) {
            existingItem.setType(itemDTO.getType());
        }
        if (itemDTO.getEventTime() != null) {
            existingItem.setEventTime(itemDTO.getEventTime());
        }
        if (itemDTO.getEventPlace() != null) {
            existingItem.setEventPlace(itemDTO.getEventPlace());
        }
        if (itemDTO.getStatus() != null) {
            existingItem.setStatus(itemDTO.getStatus());
        }
        if (itemDTO.getDescription() != null) {
            existingItem.setDescription(itemDTO.getDescription());
        }

        // 更新数据库
        itemMapper.updateById(existingItem);

        return existingItem;
    }

    @Override
    public Item getItemById(Long itemId) {
        return itemMapper.selectById(itemId);
    }

    @Override
    public PageResultVO<Item> filterItems(ItemFilter ItemFilterDTO) {
        // 创建MyBatis-Plus分页对象
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Item> page = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                ItemFilterDTO.getPageNo(), ItemFilterDTO.getPageSize());

        LambdaQueryWrapper<Item> queryWrapper = new LambdaQueryWrapper<>();

        // 类型筛选
        if (ItemFilterDTO.getType() != null) {
            queryWrapper.eq(Item::getType, ItemFilterDTO.getType());
        }

        // 状态筛选
        if (ItemFilterDTO.getStatus() != null) {
            queryWrapper.eq(Item::getStatus, ItemFilterDTO.getStatus());
        }

        // 时间段筛选
        if (ItemFilterDTO.getStartTime() != null) {
            queryWrapper.ge(Item::getCreatedAt, ItemFilterDTO.getStartTime());
        }
        if (ItemFilterDTO.getEndTime() != null) {
            queryWrapper.le(Item::getCreatedAt, ItemFilterDTO.getEndTime());
        }

        // 标签筛选
        if (ItemFilterDTO.getTags() != null && !ItemFilterDTO.getTags().isEmpty()) {
            // 先查找匹配的标签ID
            List<Tag> tags = tagMapper.selectList(
                    new LambdaQueryWrapper<Tag>().in(Tag::getName, ItemFilterDTO.getTags()));

            if (!tags.isEmpty()) {
                List<Long> tagIds = tags.stream()
                        .map(Tag::getId)
                        .collect(Collectors.toList());

                // 然后查找这些标签对应的物品ID
                List<Long> itemIds = itemTagMapper.selectList(
                        new LambdaQueryWrapper<ItemTag>().in(ItemTag::getTagId, tagIds)).stream()
                        .map(ItemTag::getItemId)
                        .distinct()
                        .collect(Collectors.toList());

                if (!itemIds.isEmpty()) {
                    queryWrapper.in(Item::getId, itemIds);
                } else {
                    // 如果没有找到匹配标签的物品，则返回空结果
                    queryWrapper.eq(Item::getId, -1L); // 一个不可能存在的ID
                }
            } else {
                // 如果没有找到匹配的标签，则返回空结果
                queryWrapper.eq(Item::getId, -1L); // 一个不可能存在的ID
            }
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Item::getCreatedAt);

        // 执行查询
        itemMapper.selectPage(page, queryWrapper);

        // 封装分页结果
        return PageUtils.toPageResult(page);
    }

    @Override
    public boolean takeDownItem(Long itemId, Long userId) {
        // 查询物品是否存在且属于当前用户
        Item existingItem = itemMapper.selectById(itemId);
        if (existingItem == null) {
            throw new AppException(ResponseCode.ITEM_NOT_FOUND.getCode(), ResponseCode.ITEM_NOT_FOUND.getInfo());
        }
        if (!existingItem.getUserId().equals(userId)) {
            throw new AppException(ResponseCode.NO_PERMISSION.getCode(), ResponseCode.NO_PERMISSION.getInfo());
        }
        if (ItemStatus.CLOSED.getCode().equals(existingItem.getStatus())) {
            throw new AppException(ResponseCode.ITEM_STATUS_INVALID.getCode(),
                    ResponseCode.ITEM_STATUS_INVALID.getInfo());
        }

        // 逻辑删除物品
        int rows = itemMapper.deleteById(itemId);

        return rows > 0;
    }

    @Value("${ai.ali.api-key:#{null}}")
    private String apiKey;

    @Override
    public String text2vec(String text) {
        try {
            // 检查API密钥是否配置
            if (apiKey == null || apiKey.trim().isEmpty()) {
                System.out.println("警告：AI API密钥未配置，无法进行文本向量化");
                throw new AppException(
                        ResponseCode.UN_ERROR.getCode(),
                        "AI API密钥未配置，请在配置文件中设置ai.ali.api-key");
            }

            // 使用API密钥构建参数
            TextEmbeddingParam param = TextEmbeddingParam
                    .builder()
                    .apiKey(apiKey)
                    .model("text-embedding-v1") // 使用文本嵌入模型
                    .texts(Arrays.asList(text))
                    .build();

            // 创建嵌入服务实例
            TextEmbedding textEmbedding = new TextEmbedding();

            // 调用API获取结果
            TextEmbeddingResult result = textEmbedding.call(param);

            // 提取向量并转换为字符串
            List<TextEmbeddingResultItem> items = result.getOutput().getEmbeddings();
            if (items != null && !items.isEmpty()) {
                List<Double> vector = items.get(0).getEmbedding();
                // 将Double转换为Float
                List<Float> floatVector = vector.stream()
                        .map(Double::floatValue)
                        .collect(Collectors.toList());
                return floatVector.toString();
            } else {
                throw new AppException(
                        ResponseCode.UN_ERROR.getCode(),
                        "向量结果为空");
            }
        } catch (Exception e) {
            System.out.println("文本向量化时发生异常：" + e.getMessage());
            e.printStackTrace();
            throw new com.whut.lostandfoundforwhut.common.exception.AppException(
                    com.whut.lostandfoundforwhut.common.enums.ResponseCode.UN_ERROR.getCode(),
                    "文本向量化失败: " + e.getMessage());
        }
    }

}
