package com.whut.lostandfoundforwhut.common.constant;

/**
 * @author DXR
 * @date 2026/01/30
 * @description 常量定义
 */
public class Constants {

    /** 逗号分隔符 */
    public static final String SPLIT = ",";
    /** 冒号分隔符 */
    public static final String COLON = ":";
    /** 空格 */
    public static final String SPACE = " ";
    /** 下划线 */
    public static final String UNDERLINE = "_";

    /**
     * @author DXR
     * @date 2026/01/30
     * @description Redis Key 规则
     */
    public static class RedisKey {
        /** Redis Key 前缀 */
        public static final String PREFIX = "lost_and_found:";
        /** 用户 Token Key 示例：lost_and_found:user:token:1234567890 */
        public static final String USER_TOKEN = PREFIX + "user:token:";
        /** 用户 Profile Key 示例：lost_and_found:user:profile:1234567890 */
        public static final String USER_PROFILE = PREFIX + "user:profile:";
        /** 物品详情 Key 示例：lost_and_found:item:detail:1234567890 */
        public static final String ITEM_DETAIL = PREFIX + "item:detail:";
        /** 物品列表 Key 示例：lost_and_found:item:list:1234567890 */
        public static final String ITEM_LIST = PREFIX + "item:list:";
        /** 物品布隆过滤器 Key 示例：lost_and_found:bloom:item */
        public static final String ITEM_BLOOM = PREFIX + "bloom:item";
    }
}
