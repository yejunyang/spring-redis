package com.yejy.springredis.redis.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.yejy.springredis.redis.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
    @Autowired
    @Qualifier("changeUserStatusScript")
    private DefaultRedisScript changeUserStatusScript;
    @Autowired
    @Qualifier("cancelForCustomerScript")
    private DefaultRedisScript cancelForCustomerScript;
    @Autowired
    private OrderService orderService;

    private final String key_pre = "{my_key_pre}_";
    private final String customer_order = key_pre + "customer:order";
    private final String customer_map = key_pre + "customer:map";
    private final String user_map = key_pre + "user:map";
    private final List<String> matchResult = Arrays.asList("offLine","noUser","noMatchUser","noMatchCustomer","noCustomer");

    public UserEntity userOnLine(UserEntity userEntity){
        userEntity.setId(UUID.randomUUID().toString());
        userEntity.setStatus(StatusEnum.IDLE.getCode());
        redisTemplate.opsForHash().put(user_map,userEntity.getId(), JSON.toJSONString(userEntity));
        redisTemplate.expire(user_map,1, TimeUnit.HOURS);
        return userEntity;
    }

    public Map<Object, Object> getAllUser(){
        return redisTemplate.opsForHash().entries(user_map);
    }

    public UserEntity getUserDetail(String id){
        String userStr = (String) redisTemplate.opsForHash().get(user_map, id);
        if (StringUtils.hasText(userStr)) {
            return JSONObject.parseObject(userStr,UserEntity.class);
        }
        return null;
    }

    public CustomerEntity customerOnline(CustomerEntity customerEntity){
        customerEntity.setId(UUID.randomUUID().toString());
        redisTemplate.opsForHash().put(customer_map,customerEntity.getId(),JSON.toJSONString(customerEntity));
        redisTemplate.expire(customer_map,1, TimeUnit.HOURS);
        redisTemplate.opsForZSet().add(customer_order,customerEntity.getId(),System.currentTimeMillis());
        redisTemplate.expire(customer_order,1, TimeUnit.HOURS);
        return customerEntity;
    }

    public Map<Object, Object> getAllCustomer(){
        return redisTemplate.opsForHash().entries(customer_map);
    }

    public CustomerEntity getCustomerDetail(String id){
        String customerStr = (String) redisTemplate.opsForHash().get(customer_map, id);
        if (StringUtils.hasText(customerStr)) {
            return JSONObject.parseObject(customerStr,CustomerEntity.class);
        }
        return null;
    }

    public RedisEntity<UserEntity> customerMatchUser(String customerId) {
        List<String> key = new ArrayList<>();
        key.add(customer_map);
        key.add(customer_order);
        key.add(user_map);
        String result = (String) redisTemplate.execute(customerMatchUserScript, key, customerId);
        return JSON.parseObject(result,new TypeReference<RedisEntity<UserEntity>>(){});
    }

    public RedisEntity<CustomerEntity> userMatchCustomer(String userId) {
        List<String> key = new ArrayList<>();
        key.add(user_map);
        key.add(customer_map);
        key.add(customer_order);
        String result = (String) redisTemplate.execute(userMatchCustomerScript, key, userId);
        return JSON.parseObject(result,new TypeReference<RedisEntity<CustomerEntity>>(){});
    }

    public String removeAll() {
        redisTemplate.delete(customer_order);
        redisTemplate.delete(customer_map);
        redisTemplate.delete(user_map);
        return "success";
    }

    public RedisEntity<UserEntity> changeUserStatus(UserEntity userEntity){
        List<String> key = new ArrayList<>();
        key.add(user_map);
        String result = (String) redisTemplate.execute(changeUserStatusScript, key,userEntity.getStatus(), userEntity.getId());
        return JSON.parseObject(result,new TypeReference<RedisEntity<UserEntity>>(){});
    }

    public RedisEntity<CustomerEntity> cancelForCustomer(CustomerEntity customerEntity){
        List<String> key = new ArrayList<>();
        key.add(customer_map);
        key.add(customer_order);
        String result = (String) redisTemplate.execute(cancelForCustomerScript, key, customerEntity.getId());
        return JSON.parseObject(result,new TypeReference<RedisEntity<CustomerEntity>>(){});
    }

    public UserEntity submit(UserEntity userEntity){
        UserEntity userDetail = getUserDetail(userEntity.getId());
        if (!StatusEnum.DOING.getCode().equals(userDetail.getStatus())){
            return null;
        }
        String customerId = userDetail.getCustomerId();
        userDetail.setStatus(StatusEnum.SUBMIT.getCode());
        userDetail.setCustomerId(null);
        redisTemplate.opsForHash().put(user_map,userEntity.getId(), JSON.toJSONString(userDetail));
        redisTemplate.expire(user_map,1, TimeUnit.HOURS);
        redisTemplate.opsForHash().delete(customer_map,customerId);
        return userDetail;
    }

    public int getCustomerOrderNum(CustomerEntity customerEntity){
        OrderEntity orderEntity = getAllUserAndAllCustomer();
        orderEntity.setCustomerOrder(redisTemplate.opsForZSet().range(customer_order, 0, -1));
        orderEntity.setCurrentId(customerEntity.getId());
        return orderService.numBeforeForCustomer(orderEntity);
    }

    public int getOrderNumForUser(UserEntity userEntity){
        OrderEntity orderEntity = getAllUserAndAllCustomer();
        orderEntity.setCurrentId(userEntity.getId());
        return orderService.getOrderForUser(orderEntity);
    }

    private OrderEntity getAllUserAndAllCustomer(){
        OrderEntity orderEntity = new OrderEntity();

        List<CustomerEntity> allCustomers = new ArrayList<>();
        Map<Object, Object> customersMap = redisTemplate.opsForHash().entries(customer_map);
        customersMap.forEach((k,v) -> allCustomers.add(JSON.parseObject((String)v,CustomerEntity.class)));
        orderEntity.setCustomers(allCustomers);

        List<UserEntity> allUsers = new ArrayList<>();
        Map<Object, Object> usersMap = redisTemplate.opsForHash().entries(user_map);
        usersMap.forEach((k,v) -> allUsers.add(JSON.parseObject((String)v,UserEntity.class)));
        orderEntity.setUsers(allUsers);

        return orderEntity;
    }
}
