package com.whut.lostandfoundforwhut.common.utils.lock;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 本地 Key 级锁（单体应用防缓存击穿）
 */
public class LocalKeyLock {
    private static final ConcurrentMap<String, ReentrantLock> LOCKS = new ConcurrentHashMap<>();

    private LocalKeyLock() {
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 获取指定 key 的锁（如不存在会创建）
     * @param key 键
     * @return ReentrantLock 锁
     */
    public static ReentrantLock getLock(String key) {
        return LOCKS.computeIfAbsent(key, k -> new ReentrantLock());
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 释放并清理锁（避免 map 无限增长）
     * @param key 键
     * @param lock 锁对象
     */
    public static void unlock(String key, ReentrantLock lock) {
        if (lock == null) {
            return;
        }
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
        if (!lock.isLocked()) {
            LOCKS.remove(key, lock);
        }
    }

    /**
     * @author DXR
     * @date 2026/01/30
     * @description 模板方法：自动加锁/解锁
     * @param key 键
     * @param supplier 执行逻辑
     * @return 执行结果
     * @param <T> 返回类型
     */
    public static <T> T withLock(String key, Supplier<T> supplier) {
        ReentrantLock lock = getLock(key);
        lock.lock();
        try {
            return supplier.get();
        } finally {
            unlock(key, lock);
        }
    }
}
