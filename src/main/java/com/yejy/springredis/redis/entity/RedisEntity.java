package com.yejy.springredis.redis.entity;

/**
 * @author yejunyang2012@163.com
 * @date 2021/10/16 20:18
 **/
public class RedisEntity<T> {
    private String type;
    private T data;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
