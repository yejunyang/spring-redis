package com.yejy.springredis.redis.controller;

import com.yejy.springredis.redis.entity.CustomerEntity;
import com.yejy.springredis.redis.entity.UserEntity;
import com.yejy.springredis.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

/**
 * @author yejunyang2012@163.com
 * @date 2021/4/11 14:05
 **/
@RestController
@RequestMapping("redis")
public class RedisController {
    @Autowired
    private RedisService redisService;


    @RequestMapping(value = "userOnline",method = RequestMethod.POST)
    public UserEntity userOnline(@RequestBody UserEntity userEntity){
        return redisService.userOnLine(userEntity);
    }

    @RequestMapping(value = "getAllUser",method = RequestMethod.GET)
    public Set<String> getAllUser(){
        return redisService.getAllUser();
    }

    @RequestMapping(value = "getUserDetail",method = RequestMethod.GET)
    public UserEntity getUserDetail(String id){
        return redisService.getUserDetail(id);
    }

    @RequestMapping(value = "customerOnline",method = RequestMethod.POST)
    public CustomerEntity customerOnline(@RequestBody CustomerEntity customerEntity){
        return redisService.customerOnline(customerEntity);
    }

    @RequestMapping(value = "getAllCustomer",method = RequestMethod.GET)
    public Set<String> getAllCustomer(){
        return redisService.getAllCustomer();
    }

    @RequestMapping(value = "getCustomerDetail",method = RequestMethod.GET)
    public CustomerEntity getCustomerDetail(String id){
        return redisService.getCustomerDetail(id);
    }

    @RequestMapping(value = "customerMatchUser",method = RequestMethod.GET)
    public UserEntity customerMatchUser(String customerId){
        return redisService.customerMatchUser(customerId);
    }

    @RequestMapping(value = "userMatchCustomer",method = RequestMethod.GET)
    public CustomerEntity userMatchCustomer(String userId){
        return redisService.userMatchCustomer(userId);
    }

    @RequestMapping(value = "removeAll",method = RequestMethod.GET)
    public String removeAll(){
        return redisService.removeAll();
    }
}
