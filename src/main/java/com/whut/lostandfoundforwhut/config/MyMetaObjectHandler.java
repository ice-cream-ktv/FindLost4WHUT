package com.whut.lostandfoundforwhut.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * @author DXR
 * @date 2026/01/30
 * @description MyBatis-Plus 自动填充（创建/更新时间）
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 插入时自动填充创建/更新时间
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, now);
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 更新时自动填充更新时间
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
    }
}
