package com.yejy.springredis.redis.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author yejunyang2012@163.com
 * @date 2021/10/17 14:52
 **/
public class OrderEntity {
    private List<CustomerEntity> customers;
    private List<UserEntity> users;
    private Set<String> customerOrder;
    private String currentId;

    public List<CustomerEntity> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerEntity> customers) {
        this.customers = customers;
    }

    public List<UserEntity> getUsers() {
        return users;
    }

    public void setUsers(List<UserEntity> users) {
        this.users = users;
    }

    public Set<String> getCustomerOrder() {
        return customerOrder;
    }

    public void setCustomerOrder(Set<String> customerOrder) {
        this.customerOrder = customerOrder;
    }

    public String getCurrentId() {
        return currentId;
    }

    public void setCurrentId(String currentId) {
        this.currentId = currentId;
    }
}
