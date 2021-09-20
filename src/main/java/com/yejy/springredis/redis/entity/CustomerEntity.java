package com.yejy.springredis.redis.entity;

import com.alibaba.fastjson.JSON;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yejunyang2012@163.com
 * @date 2021/4/11 14:09
 **/
public class CustomerEntity implements Serializable {
    private String id;
    private String name;
    private String userId;
    private List<ParamEntity> params;

    public CustomerEntity() {
    }

    public CustomerEntity(Map<Object , Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.userId = (String) map.get("userId");
        this.params = JSON.parseArray((String) map.get("params"),ParamEntity.class);
    }

    public CustomerEntity(String id, String name, List<ParamEntity> params) {
        this.id = id;
        this.name = name;
        this.params = params;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ParamEntity> getParams() {
        return params;
    }

    public void setParams(List<ParamEntity> params) {
        this.params = params;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Map<String , String> toMap(){
        Map<String , String> map = new HashMap<>(4);
        map.put("id",this.id);
        map.put("name",this.name);
        map.put("userId",this.userId);
        map.put("params", JSON.toJSONString(this.params));
        return map;
    }
}
