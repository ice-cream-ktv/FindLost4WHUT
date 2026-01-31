package com.whut.lostandfoundforwhut.common.utils.bloom.factory;

import com.whut.lostandfoundforwhut.common.utils.bloom.RedisBloomFilter;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 布隆过滤器工厂，缓存并复用实例
 */
@Component
public class BloomFilterFactory {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    private final Map<String, RedisBloomFilter> cache = new ConcurrentHashMap<>();

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 获取布隆过滤器实例（同 key 复用）
     * @param key Redis Key
     * @param expectedInsertions 预期插入数量
     * @param fpp 误判率
     * @return RedisBloomFilter 实例
     */
    public RedisBloomFilter getBloomFilter(String key, long expectedInsertions, double fpp) {
        return cache.computeIfAbsent(key, k -> new RedisBloomFilter(redisTemplate, k, expectedInsertions, fpp));
    }
}
