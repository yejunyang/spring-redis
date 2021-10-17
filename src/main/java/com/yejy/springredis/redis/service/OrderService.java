package com.yejy.springredis.redis.service;

import com.yejy.springredis.redis.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author yejunyang2012@163.com
 * @date 2021/10/17 14:48
 **/
@Service
public class OrderService {

    public int getOrderForUser(OrderEntity orderEntity){
        if (CollectionUtils.isEmpty(orderEntity.getCustomers())){
            return 0;
        }
        UserEntity currentUser = null;
        if (CollectionUtils.isEmpty(orderEntity.getUsers())){
            return 0;
        }
        for (UserEntity user : orderEntity.getUsers()) {
            if (orderEntity.getCurrentId().equals(user.getId())){
                currentUser = user;
                break;
            }
        }
        if (currentUser == null) {
            return 0;
        }
        int count = 0;
        for (CustomerEntity customer : orderEntity.getCustomers()) {
            if (StringUtils.hasText(customer.getUserId())){
                continue;
            }
            int match = match(customer.getParams(), currentUser.getParams());
            if (match > 0){
                count++;
            }
        }
        return count;
    }

    public int numBeforeForCustomer(OrderEntity orderEntity){
        if (CollectionUtils.isEmpty(orderEntity.getCustomerOrder())){
            return 0;
        }
        CustomerEntity currentCustomer = null;
        List<CustomerEntity> customerSorted = new ArrayList<>();
        for (String s : orderEntity.getCustomerOrder()) {
            for (CustomerEntity customer : orderEntity.getCustomers()) {
                if (currentCustomer == null && customer.getId().equals(orderEntity.getCurrentId())){
                    currentCustomer = customer;
                }
                if (s.equals(customer.getId())){
                    customerSorted.add(customer);
                    break;
                }
            }
        }
        if (currentCustomer == null) {
            return 0;
        }
        orderEntity.setCustomers(customerSorted);
        return matchNumForCustomer(orderEntity,currentCustomer);
    }

    private int matchNumForCustomer(OrderEntity orderEntity, CustomerEntity currentCustomer){
        List<UserEntity> matchedUsers = new ArrayList<>();
        for (UserEntity user : orderEntity.getUsers()) {
            int score = match(currentCustomer.getParams(),user.getParams());
            if (score > 0){
                matchedUsers.add(user);
            }
        }

        int count = 0;

        if (CollectionUtils.isEmpty(matchedUsers)){
            for (CustomerEntity customer : orderEntity.getCustomers()) {
                if (customer.getId().equals(currentCustomer.getId())){
                    return count;
                }
                count++;
            }
        }

        for (CustomerEntity customer : orderEntity.getCustomers()) {
            if (customer.getId().equals(currentCustomer.getId())){
                return count;
            }
            for (UserEntity matchedUser : matchedUsers) {
                int match = match(customer.getParams(), matchedUser.getParams());
                if (match > 0){
                    count++;
                }
            }
        }
        return count;
    }

    private int match(List<ParamEntity> customers,List<ParamEntity> users){
        if (CollectionUtils.isEmpty(customers) || CollectionUtils.isEmpty(users)){
            return 0;
        }
        int score = 0;
        for (ParamEntity param : customers) {
            for (ParamEntity userParam : users) {
                if (param.getKey().equals(userParam.getKey())){
                    if (ParamTypeEnum.EQUAL.getType().equals(userParam.getType())){
                        if (param.getValue().equals(userParam.getValue())){
                            score++;
                        }
                    }else if (ParamTypeEnum.COMPARE.getType().equals(userParam.getType())){
                        String[] split = userParam.getValue().split(",");
                        float max,min = 0;
                        min = Float.parseFloat(split[0].trim());
                        max = Float.parseFloat(split[split.length-1].trim());
                        float customer = Float.parseFloat(param.getValue().trim());
                        if (customer >= min && customer <= max){
                            score++;
                        }
                    }else if (ParamTypeEnum.CONTAIN.getType().equals(userParam.getType())){
                        if (userParam.getValue().contains(param.getValue())){
                            score++;
                        }
                    }
                }
            }
        }
        return score;
    }
}
