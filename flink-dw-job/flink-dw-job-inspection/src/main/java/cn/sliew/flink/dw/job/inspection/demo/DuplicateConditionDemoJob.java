package cn.sliew.flink.dw.job.inspection.demo;

import cn.sliew.flink.dw.cep.function.CustomPatternProcessFunction;
import cn.sliew.flink.dw.job.inspection.condition.DuplicateEventCondition;
import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.dynamic.impl.json.util.CepJsonUtils;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Arrays;
import java.util.Date;

public class DuplicateConditionDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> source = getSource(env);
        Pattern<Event, Event> pattern = Pattern.<Event>begin("duplicate", AfterMatchSkipStrategy.noSkip())
                // 检测同一个用户，5 分钟内，5 次事件内 action 重复次数大于 3 次
                .where(new DuplicateEventCondition(5, 5, 3));

        System.out.println(CepJsonUtils.convertPatternToJSONString(pattern));

        SingleOutputStreamOperator<String> process = CEP.pattern(source, pattern).process(new CustomPatternProcessFunction<>());

        process.print();

        env.execute();
    }

    private static SingleOutputStreamOperator<Event> getSource(StreamExecutionEnvironment env) {
        // 必须设置 watermark
        return env.fromCollection(
                        Arrays.asList(
                                new Event(1, 1, "ken", 0, 1662022777000L), // 2022-09-01 16:59:37
                                new Event(2, 1, "ken", 0, 1662022778000L),
                                new Event(3, 1, "ken", 0, 1662022779000L),
                                new Event(4, 1, "ken", 2, 1662022780000L),
                                new Event(5, 1, "ken", 1, 1662022790000L), // 2022-09-01 16:59:50
                                new Event(6, 1, "ken", 1, 1662022900000L), // 2022-09-01 17:01:40
                                new Event(7, 1, "ken", 1, 1662023000000L), // 2022-09-01 17:03:20
                                new Event(8, 1, "ken", 1, 1662023100000L), // 2022-09-01 17:05:00
                                new Event(9, 1, "ken", 1, 1662023110000L), // 2022-09-01 17:05:10
                                new Event(10, 1, "ken", 1, 1662023300000L), // 2022-09-01 17:08:20
                                new Event(11, 1, "ken", 1, 1662023310000L), // 2022-09-01 17:08:30
                                new Event(12, 1, "ken", 4, 1662023311000L)
//                                new Event(13, 1, "ken", 5, 1662122790000L),
//                                new Event(14, 1, "ken", 6, 1662122790000L),
//                                new Event(15, 1, "ken", 7, 1662122790000L)
                        )
                )
                .assignTimestampsAndWatermarks(WatermarkStrategy.
                        <Event>forMonotonousTimestamps().withTimestampAssigner((event, ts) -> event.getTimestamp()));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        private int id;
        private int userId;
        private String name;
        private int action;
        private long timestamp;

        @Override
        public String toString() {
            return "Event{" +
                    "id=" + id +
                    ", timestamp=" + DateFormatUtils.format(new Date(timestamp), "yyyy-MM-dd HH:mm:ss") +
                    '}';
        }
    }
}
