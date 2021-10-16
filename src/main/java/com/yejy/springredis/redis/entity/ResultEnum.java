package com.yejy.springredis.redis.entity;

/**
 * @author yejunyang2012@163.com
 * @date 2021/10/16 22:18
 **/
public enum ResultEnum {
    /**
     * 成功
     */
    SUCCESSFUL("SUCCESSFUL","成功"),
    /**
     * 没有匹配成功用户
     */
    NO_MATCH_USER("NO_MATCH_USER","没有匹配成功用户"),
    /**
     * 客户参数错误
     */
    CUSTOMER_PARAM_ERROR("CUSTOMER_PARAM_ERROR","客户参数错误"),
    /**
     * 没有用户
     */
    NO_USER("NO_USER","没有用户"),
    /**
     * 已经下线
     */
    ALREADY_OFFLINE("ALREADY_OFFLINE","已经下线"),
    /**
     * 没有匹配的客户
     */
    NO_MATCH_CUSTOMER("NO_MATCH_CUSTOMER","没有匹配的客户"),
    /**
     * 没有客户
     */
    NO_CUSTOMER("NO_CUSTOMER","没有客户"),
    ;

    private String code;
    private String des;

    private ResultEnum(String code, String des) {
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
