package com.yejy.springredis.redis.entity;

/**
 * @author yejunyang2012@163.com
 * @date 2021/10/16 22:44
 **/
public enum StatusEnum {
    /**
     * 空闲
     */
    IDLE("IDLE","空闲"),
    /**
     * 忙
     */
    DONG("DONG","忙"),
    /**
     * 提交
     */
    SUBMIT("SUBMIT","提交"),
    /**
     * 下线
     */
    OFFLINE("OFFLINE","下线"),
    ;
    private String code;
    private String des;


    StatusEnum(String code, String des) {
        this.code = code;
        this.des = des;
    }

    public String getCode() {
        return code;
    }

    public String getDes() {
        return des;
    }
}
