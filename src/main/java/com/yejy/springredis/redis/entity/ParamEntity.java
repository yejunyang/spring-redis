package com.yejy.springredis.redis.entity;

import java.io.Serializable;

/**
 * @author yejunyang2012@163.com
 * @date 2021/4/11 14:09
 **/
public class ParamEntity implements Serializable {
    private String key;
    private String type;
    private String value;

    public ParamEntity(String key, String type, String value) {
        this.key = key;
        this.type = type;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
