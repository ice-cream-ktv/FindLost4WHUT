package com.whut.lostandfoundforwhut.common.utils.bloom;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;

/**
 * @author DXR
 * @date 2026/01/30
 * @description Redis Bitmap 布隆过滤器（多哈希）
 */
public class RedisBloomFilter {
    private final RedisTemplate<String, Object> redisTemplate;
    private final String key;
    private final long bitSize;
    private final int hashFunctions;

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 构造布隆过滤器
     * @param redisTemplate RedisTemplate
     * @param key Redis Key
     * @param expectedInsertions 预期插入数量
     * @param fpp 误判率（例如 0.01）
     */
    public RedisBloomFilter(RedisTemplate<String, Object> redisTemplate, String key, long expectedInsertions, double fpp) {
        this.redisTemplate = redisTemplate;
        this.key = key;
        this.bitSize = optimalNumOfBits(expectedInsertions, fpp);
        this.hashFunctions = optimalNumOfHashFunctions(expectedInsertions, bitSize);
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 添加元素
     * @param value 元素值
     */
    public void add(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("布隆过滤器添加的元素不能为null/空字符串");
        }
        long[] hashes = hash(value);
        long hash1 = hashes[0];
        long hash2 = hashes[1];
        try {
            for (int i = 0; i < hashFunctions; i++) {
                long combined = hash1 + (long) i * hash2;
                // combined 可能为负数，& Long.MAX_VALUE 转为正数，避免取模后索引为负
                long index = (combined & Long.MAX_VALUE) % bitSize;
                redisTemplate.opsForValue().setBit(key, index, true);
            }
        } catch (DataAccessException e) {
            throw new RuntimeException("Redis布隆过滤器添加元素失败", e);
        }
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 判断是否可能存在（可能误判）
     * @param value 元素值
     * @return 是否可能存在
     */
    public boolean mightContain(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        long[] hashes = hash(value);
        long hash1 = hashes[0];
        long hash2 = hashes[1];
        try {
            for (int i = 0; i < hashFunctions; i++) {
                long combined = hash1 + (long) i * hash2;
                // combined 可能为负数，& Long.MAX_VALUE 转为正数，避免取模后索引为负
                long index = (combined & Long.MAX_VALUE) % bitSize;
                Boolean bit = redisTemplate.opsForValue().getBit(key, index);
                if (bit == null || !bit) {
                    return false;
                }
            }
            return true;
        } catch (DataAccessException e) {
            throw new RuntimeException("Redis布隆过滤器查询失败", e);
        }
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 获取位图大小
     * @return 位图大小（bit）
     */
    public long getBitSize() {
        return bitSize;
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 获取哈希函数数量
     * @return 哈希函数数量
     */
    public int getHashFunctions() {
        return hashFunctions;
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 计算最优位图大小（m）
     * @param n 预期插入数量
     * @param p 误判率
     * @return 位图大小
     */
    private static long optimalNumOfBits(long n, double p) {
        if (p == 0) {
            p = Double.MIN_VALUE;
        }
        return (long) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 计算最优哈希函数数量（k）
     * @param n 预期插入数量
     * @param m 位图大小
     * @return 哈希函数数量
     */
    private static int optimalNumOfHashFunctions(long n, long m) {
        return Math.max(1, (int) Math.round((double) m / n * Math.log(2)));
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 生成双哈希（用于组合哈希）
     * @param value 元素值
     * @return 两个 64 位哈希值
     */
    private static long[] hash(String value) {
        byte[] data = value.getBytes(StandardCharsets.UTF_8);
        long h1 = fnv1a64(data, 0xcbf29ce484222325L);
        long h2 = fnv1a64(data, 0x100000001b3L);
        return new long[]{h1, h2};
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description FNV-1a 64 位哈希
     * @param data 字节数组
     * @param seed 初始种子
     * @return 64 位哈希值
     */
    private static long fnv1a64(byte[] data, long seed) {
        long hash = seed;
        for (byte b : data) {
            hash ^= (b & 0xff);
            hash *= 0x100000001b3L;
        }
        return hash;
    }
}
