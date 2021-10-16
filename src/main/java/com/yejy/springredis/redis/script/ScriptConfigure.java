package com.yejy.springredis.redis.script;

import com.yejy.springredis.redis.entity.RedisEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

/**
 * @author yejunyang2012@163.com
 * @date 2021/4/11 15:26
 **/
@Configuration
public class ScriptConfigure {
    @Bean("customerMatchUserScript")
    public DefaultRedisScript<String> customerMatchUserScript() {
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(String.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/customerMatchUser.lua")));
        return defaultRedisScript;
    }

    @Bean("userMatchCustomerScript")
    public DefaultRedisScript<String> userMatchCustomerScript() {
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(String.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/userMatchCustomer.lua")));
        return defaultRedisScript;
    }

    @Bean("changeUserStatusScript")
    public DefaultRedisScript<String> changeUserStatus() {
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(String.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/changeUserStatus.lua")));
        return defaultRedisScript;
    }

    @Bean("cancelForCustomerScript")
    public DefaultRedisScript<String> cancelForCustomerScript() {
        DefaultRedisScript<String> defaultRedisScript = new DefaultRedisScript<>();
        defaultRedisScript.setResultType(String.class);
        defaultRedisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/cancelForCustomer.lua")));
        return defaultRedisScript;
    }
}
