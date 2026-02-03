package com.whut.lostandfoundforwhut.mapper;

import com.whut.lostandfoundforwhut.model.entity.Image;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ItemImageMapperTest {

    @Autowired
    private ItemImageMapper itemImageMapper;

    @Test
    void testMethodsExist() {
        // 验证方法可以调用
        List<Image> images = itemImageMapper.getImagesByItemId(1L);
        List<String> urls = itemImageMapper.getImageUrlsByItemId(1L);

        System.out.println(images);
        System.out.println(urls);
        
        assert images != null;
        assert urls != null;
    }
}

