package com.ecommerce.auth.infrastructure.config;

import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Bucket4j rate-limiting configuration.
 *
 * Creates its own Lettuce RedisClient + StatefulRedisConnection with explicit
 * String/byte[] codec so that Bucket4j's byte-based serialization works correctly.
 */
@Configuration
public class Bucket4jConfig {

    @Bean(destroyMethod = "shutdown")
    public RedisClient bucket4jRedisClient(
        @Value("${REDIS_HOST:localhost}") String host,
        @Value("${REDIS_PORT:6379}") int port,
        @Value("${REDIS_PASSWORD:}") String password) {

        RedisURI.Builder builder = RedisURI.builder()
                .withHost(host)
                .withPort(port)
                .withSsl(!host.equals("localhost"));

        if (password != null && !password.isBlank()) {
            builder.withPassword(password.toCharArray());
        }

        return RedisClient.create(builder.build());
    }

    @Bean
    public LettuceBasedProxyManager<String> lettuceBasedProxyManager(RedisClient bucket4jRedisClient) {
        RedisCodec<String, byte[]> codec = RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE);
        StatefulRedisConnection<String, byte[]> connection = bucket4jRedisClient.connect(codec);
        return LettuceBasedProxyManager.builderFor(connection)
                .build();
    }
}