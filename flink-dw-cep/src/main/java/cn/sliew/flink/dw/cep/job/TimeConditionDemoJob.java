package cn.sliew.flink.dw.cep.job;

import cn.sliew.flink.dw.cep.condition.TimeCondition;
import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.dynamic.condition.AviatorCondition;
import org.apache.flink.cep.dynamic.impl.json.util.CepJsonUtils;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeConditionDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> source = getSource(env);
        Pattern<Event, Event> pattern = Pattern.<Event>begin("start", AfterMatchSkipStrategy.noSkip())
                .where(new AviatorCondition<>("action == 0"))
                .where(new TimeCondition())
                .followedBy("end")
                .oneOrMore()
                // action != 1 and action != 0
                .where(new AviatorCondition<>("action != 1"))
                .where(new AviatorCondition<>("action != 0"))
                .within(Time.of(1L, TimeUnit.MINUTES))
                ;

        System.out.println(CepJsonUtils.convertPatternToJSONString(pattern));

        SingleOutputStreamOperator<String> process = CEP.pattern(source, pattern).process(new PatternProcessFunction<Event, String>() {
            @Override
            public void processMatch(Map<String, List<Event>> match, Context context, Collector<String> out) throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("A match for Pattern is found. The event sequence: ");
                for (Map.Entry<String, List<Event>> entry : match.entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(", ");
                }
                out.collect(sb.toString());
            }
        });

        process.print();

        env.execute();
    }

    private static SingleOutputStreamOperator<Event> getSource(StreamExecutionEnvironment env) {
        // 必须设置 watermark
        return env.fromCollection(
                        Arrays.asList(
                                new Event(1, 1, "ken", 0, 1662022777000L),
                                new Event(2, 1, "ken", 0, 1662022778000L),
                                new Event(3, 1, "ken", 1, 1662022779000L),
                                new Event(4, 1, "ken", 2, 1662022780000L),
                                new Event(5, 1, "ken", 1, 1662022780000L)
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
