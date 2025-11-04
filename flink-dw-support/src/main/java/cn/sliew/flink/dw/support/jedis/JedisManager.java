package cn.sliew.flink.dw.support.jedis;

import cn.sliew.flink.dw.support.config.RedisConfig;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashMap;
import java.util.Map;

public class JedisManager {

    private static Map<RedisConfig, JedisPool> pools = new HashMap<>();

    public static Jedis getJedis(RedisConfig redisConfig) {
        if (redisConfig == null) {
            return null;
        }

        JedisPool jedisPool = pools.get(redisConfig);
        if (jedisPool != null) {
            return jedisPool.getResource();
        } else {
            return createJedisPool(redisConfig).getResource();
        }
    }

    public static void initPool(RedisConfig redisConfig) {
        createJedisPool(redisConfig);
    }

    synchronized private static JedisPool createJedisPool(RedisConfig redisConfig) {
        JedisPool jedisPool = pools.get(redisConfig);
        if (jedisPool != null) {
            return jedisPool;
        } else {
            JedisPoolConfig poolConfig = new JedisPoolConfig();
            poolConfig.setMaxTotal(100);
            poolConfig.setMaxIdle(10);
            poolConfig.setMinIdle(10);
            poolConfig.setMaxWaitMillis(10000);
            poolConfig.setTestOnBorrow(true);

            jedisPool = new JedisPool(poolConfig,
                    redisConfig.getHost(),
                    redisConfig.getPort(),
                    10000,
                    redisConfig.getPasswd(),
                    redisConfig.getDatabase());

            pools.put(redisConfig, jedisPool);

            return jedisPool;
        }
    }

    public static void returnResource(RedisConfig redisConfig, Jedis jedis) {
        JedisPool jedisPool = pools.get(redisConfig);
        if (jedisPool != null) {
            jedisPool.returnResource(jedis);
        }
    }
}
