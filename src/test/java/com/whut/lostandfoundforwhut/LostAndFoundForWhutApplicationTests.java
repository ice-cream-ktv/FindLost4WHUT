package com.whut.lostandfoundforwhut;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 应用上下文加载测试
 */
@SpringBootTest(properties = {
        "spring.autoconfigure.exclude=" +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration," +
                "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration," +
                "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration"
})
class LostAndFoundForWhutApplicationTests {

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 测试 Spring 上下文能否正常启动
     */
    @Test
    void contextLoads() {

    }

}
