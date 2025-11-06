package cn.sliew.flink.dw.job.inspection.condition;

import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.StateTtlConfig;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.pattern.conditions.RichIterativeCondition;
import org.apache.flink.configuration.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.flink.shaded.guava30.com.google.common.base.Preconditions.checkArgument;

public class DuplicateMessageBackupCondition<T> extends RichIterativeCondition<T> {

    private MapState<String, List<T>> recentEventMap;

    // 间隔 < 5 分钟，间隔 < 5 句，重复次数 > 3 次。
    private long periodTime;
    private long periodCnt;
    private long threshold;

    public DuplicateMessageBackupCondition(long periodTime, long periodCnt, long threshold) {
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
        StateTtlConfig ttlConfig = StateTtlConfig
                .newBuilder(Time.days(1))
                .setUpdateType(StateTtlConfig.UpdateType.OnCreateAndWrite)
                .setStateVisibility(StateTtlConfig.StateVisibility.NeverReturnExpired)
                .build();
        MapStateDescriptor<String, List<T>> recentEventMSD = new MapStateDescriptor<>("recentEventMap",
                TypeInformation.of(new TypeHint<String>() {
                }),
                TypeInformation.of(new TypeHint<List<T>>() {
                }));
        recentEventMSD.enableTimeToLive(ttlConfig);
        recentEventMap = getRuntimeContext().getMapState(recentEventMSD);
    }

    @Override
    public boolean filter(T event, Context<T> context) throws Exception {
        // 业务主键作为 key，记录在最近的对话中的
        // 以在聊天记录中的客服id 为例：必须是客服，然后是房间id + 客服id 即为主键
        String key = event.toString();
        List<T> recentEvents = recentEventMap.get(key);
        if (CollectionUtils.isEmpty(recentEvents)) {
            recentEvents = new ArrayList<>();
        }
        recentEvents.add(event);
        // todo 测试 eventTime 和 processTime，后续实现数据的自动驱逐，减少缓存的数据量
        long currentTimestamp = context.timestamp(); // 事件时间，如果设置的是处理时间，这里设置的是 cep 算子的 ingesttime
        recentEvents.removeIf(item -> true); // todo 检测是否在指定时间范围内，如 5 分钟，超过 5 分钟的数据全部清除掉
        recentEventMap.put(key, recentEvents);
        // 判断是否是重复消息
        return isDuplicate(recentEvents);
    }

    private boolean isDuplicate(List<T> recentEvents) {
        if (CollectionUtils.isEmpty(recentEvents)) {
            return false;
        }
        Map<String, Long> countMap = new HashMap<>();
        for (T item : recentEvents) {
            Long count = 1L;
            String message = item.toString();
            if (countMap.containsKey(message)) { // 重复，后续可能还有连续 n 句包含
                count = countMap.get(message) + 1L;
            }
            countMap.put(message, count);
        }
        return countMap.values().stream().anyMatch(count -> count > threshold);
    }

}
