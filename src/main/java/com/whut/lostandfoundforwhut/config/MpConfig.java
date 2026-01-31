package com.whut.lostandfoundforwhut.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author DXR
 * @date 2026/01/30
 * @description MyBatis-Plus 配置（分页拦截器）
 */
@Configuration
public class MpConfig {
    /**
     * @author DXR
     * @date 2026/01/30
     * @description 配置 MP 拦截器，启用分页能力
     * @return MybatisPlusInterceptor 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        // 定义 MP 拦截器
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 分页拦截器
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
