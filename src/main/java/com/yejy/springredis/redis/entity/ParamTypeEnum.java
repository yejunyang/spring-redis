package com.yejy.springredis.redis.entity;

/**
 * @author yejunyang2012@163.com
 * @date 2021/9/20 14:26
 **/
public enum ParamTypeEnum {
    /**
     * 字符串相等
     */
    EQUAL("EQUAL","字符串相等"),

    /**
     * 字符串包含
     */
    CONTAIN("CONTAIN","字符串包含"),

    /**
     * 数值比较
     */
    COMPARE("COMPARE","数值比较"),
    ;


    private String type;
    private String desc;

    public String getDesc() {
        return desc;
    }

    public String getType() {
        return type;
    }

    ParamTypeEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }
}
