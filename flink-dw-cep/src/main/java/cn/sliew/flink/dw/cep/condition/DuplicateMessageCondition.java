package cn.sliew.flink.dw.cep.condition;

import cn.sliew.flink.dw.common.JacksonUtil;
import cn.sliew.flink.dw.support.config.RedisConfig;
import cn.sliew.flink.dw.support.jedis.JedisManager;
import cn.sliew.flink.dw.support.jedis.JedisUtil;
import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.pattern.conditions.RichIterativeCondition;
import org.apache.flink.configuration.Configuration;
import redis.clients.jedis.Jedis;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.flink.shaded.guava30.com.google.common.base.Preconditions.checkArgument;

public class DuplicateMessageCondition<T> extends RichIterativeCondition<T> {

    private DuplicateElementChecker<String> checker;

    // 间隔 < 5 分钟，间隔 < 5 句，重复次数 > 3 次。
    private long periodTime;
    private int periodCnt;
    private int threshold;

    private RedisConfig redisConfig;
    private Jedis jedis;

    public DuplicateMessageCondition(long periodTime, int periodCnt, int threshold) {
        checkArgument(periodTime > 0L && periodTime < Duration.ofDays(1).toMinutes(), "时间间隔需大于 0 小于 1 天");
        checkArgument(periodCnt > 0L && periodCnt < 1000L, "语句次数需大于 0 小于 1000");
        checkArgument(threshold > 0L && threshold < 100L, "重复次数需大于 0 小于 100");
        this.periodTime = periodTime;
        this.periodCnt = periodCnt;
        this.threshold = threshold;
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        ParameterTool parameterTool = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        redisConfig = ParameterToolUtil.getRedisConfig(parameterTool, RedisConfig.DEFAULT_INSTANCE);
        jedis = JedisManager.getJedis(redisConfig);
    }

    @Override
    public void close() throws Exception {
        if (Objects.nonNull(jedis)) {
            jedis.close();
        }
    }

    @Override
    public boolean filter(T event, Context<T> context) throws Exception {
        String key = getRedisKey(event);
        long currentTimestamp = context.timestamp(); // 事件时间，如果设置的是处理时间，这里设置的是 cep 算子的 ingesttime
        jedis.zadd(key, currentTimestamp, JacksonUtil.toJsonString(event));
        long minTimestamp = currentTimestamp - Duration.ofMinutes(periodTime).toMillis();
        // 移除不在指定时间范围内的数据，如 5 分钟，超过 5 分钟的数据全部清除掉
        jedis.zremrangeByScore(key, 0, minTimestamp);
        jedis.expire(key, JedisUtil.DUPLICATE_MESSAGE_EXPIRATION);

        List<String> allElements = jedis.zrange(key, 0, -1);
        if (CollectionUtils.isEmpty(allElements)) {
            allElements = new ArrayList<>();
        }
        List<T> recentEvents = new ArrayList<>(allElements.size());
        for (int i = 0; i < allElements.size(); i++) {
            String element = allElements.get(i);
            // todo 类型
            T item = (T) JacksonUtil.parseJsonString(element, Object.class);
            recentEvents.add(item);
        }
        // 判断是否是重复消息
        return isDuplicate(recentEvents);
    }

    private boolean isDuplicate(List<T> recentEvents) {
        if (CollectionUtils.isEmpty(recentEvents)) {
            return false;
        }

        // todo 抽取具体的检测字段值
        String[] array = recentEvents.stream().map(event -> event.toString()).toArray(String[]::new);
        return checker.hasDuplicateInWindow(array, periodCnt, threshold);
    }

    private String getRedisKey(T event) {
        // 业务主键作为 key，记录在最近的对话中的
        // todo 以在聊天记录中的客服id 为例：必须是客服，然后是房间id + 客服id 即为主键
        String key = event.toString();
        return JedisUtil.DUPLICATE_MESSAGE_KEY + key;
    }

}
