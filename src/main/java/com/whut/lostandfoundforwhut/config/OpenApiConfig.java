package com.whut.lostandfoundforwhut.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author DXR
 * @date 2026/01/30
 * @description OpenAPI 基础配置
 */
@Configuration
public class OpenApiConfig {

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 构建 OpenAPI 文档信息
     * @return OpenAPI 文档对象
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LostAndFound API")
                        .version("1.0.0")
                        .description("失物招领系统 API 文档"));
    }
}
