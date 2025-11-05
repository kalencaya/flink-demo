package cn.sliew.flink.dw.cep.condition;

import cn.sliew.flink.dw.cep.job.DuplicateConditionDemoJob;
import cn.sliew.flink.dw.support.jedis.JedisUtil;
import org.apache.commons.lang3.RandomStringUtils;

public class DuplicateEventCondition extends DuplicateCheckCondition<DuplicateConditionDemoJob.Event> {

    // fixme 主要解决重新启动时，回放数据时，redis 的 key 一致，直接写入原来的数据去了
    // fixme 但是如果没有设置新的消费位点，而是选择 checkpoint 或 savepoint 启动，又回出现问题，因为就是要写入原来的数据
    // fixme 因此用到这个的任务每次都需要设置启动位点，重新启动，不能用 checkpoint 或 savepoint 启动
    private static final String RANDOM = RandomStringUtils.randomAlphanumeric(5);

    public DuplicateEventCondition(long periodTime, int periodCnt, int threshold) {
        super(periodTime, periodCnt, threshold);
    }

    @Override
    protected Class<DuplicateConditionDemoJob.Event> getEventClass() {
        return DuplicateConditionDemoJob.Event.class;
    }

    @Override
    protected String getRedisKey(DuplicateConditionDemoJob.Event event) {
        String key = Integer.toString(event.getUserId());
        return JedisUtil.DUPLICATE_MESSAGE_KEY + RANDOM + ":" + key;
    }

    @Override
    protected String getCheckString(DuplicateConditionDemoJob.Event event) {
        return Integer.toString(event.getAction());
    }
}
