package com.yejy.springredis.redis.service;

import com.yejy.springredis.redis.entity.CustomerEntity;
import com.yejy.springredis.redis.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author yejunyang2012@163.com
 * @date 2021/4/11 14:14
 **/
@Service
public class RedisService {
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    @Qualifier("customerMatchUserScript")
    private DefaultRedisScript customerMatchUserScript;
    @Autowired
    @Qualifier("userMatchCustomerScript")
    private DefaultRedisScript userMatchCustomerScript;
    private final String key_pre = "{my_key_pre}_";
    private final String customer_order = key_pre + "customer:order";
    private final String customer_map = key_pre + "customer:map:";
    private final String user_order = key_pre + "user:order";
    private final String user_map = key_pre + "user:map:";
    private final List<String> matchResult = Arrays.asList("offLine","noUser","noMatchUser");

    public UserEntity userOnLine(UserEntity userEntity){
        userEntity.setId(UUID.randomUUID().toString());
        redisTemplate.opsForHash().putAll(user_map+userEntity.getId(),userEntity.toMap());
        redisTemplate.expire(user_map+userEntity.getId(),1, TimeUnit.HOURS);
        redisTemplate.opsForZSet().add(user_order,userEntity.getId(),System.currentTimeMillis());
        redisTemplate.expire(user_order,1, TimeUnit.HOURS);
        return userEntity;
    }

    public Set<String> getAllUser(){
        return redisTemplate.opsForZSet().range(user_order,0,-1);
    }

    public UserEntity getUserDetail(String id){
        Map entries = redisTemplate.opsForHash().entries(user_map + id);
        if (entries == null || entries.size() == 0) {
            return null;
        }
        return new UserEntity(entries);
    }

    public CustomerEntity customerOnline(CustomerEntity customerEntity){
        customerEntity.setId(UUID.randomUUID().toString());
        redisTemplate.opsForHash().putAll(customer_map+customerEntity.getId(),customerEntity.toMap());
        redisTemplate.expire(customer_map+customerEntity.getId(),1, TimeUnit.HOURS);
        redisTemplate.opsForZSet().add(customer_order,customerEntity.getId(),System.currentTimeMillis());
        redisTemplate.expire(customer_order,1, TimeUnit.HOURS);
        return customerEntity;
    }

    public Set<String> getAllCustomer(){
        return redisTemplate.opsForZSet().range(customer_order,0,-1);
    }

    public CustomerEntity getCustomerDetail(String id){
        Map entries = redisTemplate.opsForHash().entries(customer_map + id);
        if (entries == null || entries.size() == 0) {
            return null;
        }
        return new CustomerEntity(entries);
    }

    public UserEntity customerMatchUser(String customerId) {
        List<String> key = new ArrayList<>();
        key.add(customer_map+customerId);
        key.add(customer_order);
        key.add(user_order);
        String result = (String) redisTemplate.execute(customerMatchUserScript, key, customerId,user_map,"1");
        if (matchResult.contains(result)) {
            return null;
        }
        return getUserDetail(result+"");
    }

    public CustomerEntity userMatchCustomer(String userId) {
        return null;
    }

    public String removeAll() {
        Set<String> allCustomer = getAllCustomer();
        if (!CollectionUtils.isEmpty(allCustomer)){
            allCustomer.forEach(id -> redisTemplate.delete(customer_map+id));
        }
        Set<String> allUser = getAllUser();
        if (!CollectionUtils.isEmpty(allUser)){
            allUser.forEach(id -> redisTemplate.delete(user_map+id));
        }
        redisTemplate.delete(customer_order);
        redisTemplate.delete(user_order);
        return "success";
    }
}
