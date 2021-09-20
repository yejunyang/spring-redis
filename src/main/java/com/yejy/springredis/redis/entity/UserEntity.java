package com.yejy.springredis.redis.entity;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.util.JSONPObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yejunyang2012@163.com
 * @date 2021/4/11 14:08
 **/
public class UserEntity implements Serializable {

    private String id;
    private String name;
    private String customerId;
    private List<ParamEntity> params;

    public UserEntity(String id, String name, List<ParamEntity> params) {
        this.id = id;
        this.name = name;
        this.params = params;
    }

    public UserEntity(Map<Object , Object> map) {
        this.id = (String) map.get("id");
        this.name = (String) map.get("name");
        this.customerId = (String) map.get("customerId");
        this.params = JSON.parseArray((String) map.get("params"),ParamEntity.class);
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

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public Map<String , String> toMap(){
        Map<String , String> map = new HashMap<>(4);
        map.put("id",this.id);
        map.put("name",this.name);
        map.put("customerId",this.customerId);
        map.put("params", JSON.toJSONString(this.params));
        return map;
    }

}
