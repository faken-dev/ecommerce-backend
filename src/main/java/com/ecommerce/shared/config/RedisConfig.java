package com.ecommerce.shared.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Value("${REDIS_HOST:localhost}")
    private String host;

    @Value("${REDIS_PORT:6379}")
    private int port;

    @Value("${REDIS_PASSWORD:}")
    private String password;


    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // Dùng rediss:// (SSL) khi không phải localhost
        String protocol = host.equals("localhost") ? "redis://" : "rediss://";
        
        config.useSingleServer()
                .setAddress(protocol + host + ":" + port)
                .setPassword(password.isEmpty() ? null : password)
                .setTimeout(3000)
                .setConnectTimeout(3000)
                .setRetryAttempts(3)
                .setRetryInterval(1500);

        return Redisson.create(config);
    }

    // RedisTemplate<String, String> used for blacklist, OTP counter
    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new StringRedisSerializer());
        template.afterPropertiesSet();
        return template;
    }
}