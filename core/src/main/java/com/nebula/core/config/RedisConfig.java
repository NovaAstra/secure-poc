package com.nebula.core.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 */
@Configuration
public class RedisConfig {

  @Bean
  RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
    Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

    PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
        .allowIfBaseType(Object.class)
        .build();
    objectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_CONCRETE_AND_ARRAYS);

    jackson2JsonRedisSerializer.setObjectMapper(objectMapper);

    RedisTemplate<Object, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);

    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(jackson2JsonRedisSerializer);
    template.setHashKeySerializer(jackson2JsonRedisSerializer);
    template.setHashValueSerializer(jackson2JsonRedisSerializer);

    template.afterPropertiesSet();
    return template;
  }
}
