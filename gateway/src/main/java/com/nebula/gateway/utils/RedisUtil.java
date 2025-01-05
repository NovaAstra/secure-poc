package com.nebula.gateway.utils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import cn.hutool.core.collection.CollUtil;

@Component
public class RedisUtil {
  private final RedisTemplate<Object, Object> redisTemplate;

  public RedisUtil(RedisTemplate<Object, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /**
   * 删除缓存
   *
   * @param keys 可以传一个或多个键
   * @return 是否删除成功
   */
  public boolean delete(Object... keys) {
    if (keys == null || keys.length == 0) {
      return false;
    }

    if (keys.length == 1) {
      return Boolean.TRUE.equals(redisTemplate.delete(keys[0]));
    }

    List<Object> keyList = CollUtil.newArrayList(keys);
    Long deletedCount = redisTemplate.delete(keyList);
    return deletedCount != null && deletedCount > 0;
  }

  /**
   * 获取缓存值
   *
   * @param key 键
   * @return 值
   */
  public Object get(String key) {
    return key == null ? null : redisTemplate.opsForValue().get(key);
  }

  /**
   * 设置缓存值
   *
   * @param key   键
   * @param value 值
   * @return 是否设置成功
   */
  public boolean set(Object key, Object value) {
    try {
      redisTemplate.opsForValue().set(key, value);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 设置缓存值并指定过期时间
   *
   * @param key   键
   * @param value 值
   * @param time  过期时间（秒）
   * @return 是否设置成功
   */
  public boolean set(Object key, Object value, long time) {
    try {
      if (time > 0) {
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
      } else {
        set(key, value);
      }
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 设置键的过期时间
   *
   * @param key  键
   * @param time 过期时间（秒）
   * @return 是否设置成功
   */
  public boolean expire(Object key, long time) {
    try {
      if (time > 0) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, time, TimeUnit.SECONDS));
      }
      return false;
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }

  /**
   * 设置缓存值（如果键不存在）
   *
   * @param key   键
   * @param value 值
   * @param time  过期时间（秒）
   * @return 是否设置成功
   */
  public boolean setIfAbsent(Object key, Object value, long time) {
    try {
      if (time > 0) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, time, TimeUnit.SECONDS));
      } else {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
      }
    } catch (Exception e) {
      e.printStackTrace();
      return false;
    }
  }
}
