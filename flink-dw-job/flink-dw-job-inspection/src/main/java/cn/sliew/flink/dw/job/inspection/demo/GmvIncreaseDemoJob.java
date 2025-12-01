package cn.sliew.flink.dw.job.inspection.demo;

import cn.sliew.flink.dw.cep.function.CustomPatternProcessFunction;
import cn.sliew.flink.dw.common.JacksonUtil;
import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.dynamic.impl.json.util.CepJsonUtils;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.BooleanConditions;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class GmvIncreaseDemoJob {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        SingleOutputStreamOperator<Event> source = getSource(env);
        Pattern<Event, Event> pattern = Pattern.<Event>begin("start", AfterMatchSkipStrategy.noSkip())
                .where(BooleanConditions.trueFunction())
                .times(3)
                .followedBy("avg")
                .oneOrMore()
                .until(new IterativeCondition<Event>() {
                    @Override
                    public boolean filter(Event event, Context<Event> context) throws Exception {
                        Iterable<Event> start = context.getEventsForPattern("start");
                        Long sum = 0L;
                        Long size = 0L;
                        for (Event startEvent : start) {
                            sum += startEvent.getGmv();
                            size++;
                        }
                        Double avg = sum * 1.0 / size;
                        double increase = (event.getGmv() - avg) * 1.0 / avg;
                        System.out.println("start: " + JacksonUtil.toJsonString(start) + ", event: " + JacksonUtil.toJsonString(event));
                        return increase > 0.2;
                    }
                });

        System.out.println(CepJsonUtils.convertPatternToJSONString(pattern));

        SingleOutputStreamOperator<String> process = CEP.pattern(source, pattern).process(new CustomPatternProcessFunction<>());

        process.print();

        env.execute();
    }

    private static SingleOutputStreamOperator<Event> getSource(StreamExecutionEnvironment env) {
        // 必须设置 watermark
        return env.fromCollection(
                        Arrays.asList(
                                new Event("20241011", 10L, "2024-10-11 00:00:00"),
                                new Event("20241012", 10L, "2024-10-12 00:00:00"),
                                new Event("20241013", 10L, "2024-10-13 00:00:00"),
                                new Event("20241014", 10L, "2024-10-14 00:00:00"),
                                new Event("20241015", 10L, "2024-10-15 00:00:00"),
                                new Event("20241016", 10L, "2024-10-15 00:00:00"),
                                new Event("20241017", 10L, "2024-10-15 00:00:00"),
                                new Event("20241018", 13L, "2024-10-15 00:00:00")
                        )
                )
                .assignTimestampsAndWatermarks(WatermarkStrategy.
                        <Event>forMonotonousTimestamps().withTimestampAssigner((event, ts) -> {
                    LocalDateTime localDateTime = LocalDateTime.parse(event.getTimestamp(), formatter);
                    return localDateTime.atZone(ZoneId.of("Asia/Shanghai"))
                            .toInstant()
                            .toEpochMilli();
                }));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        private String name;
        private Long gmv;
        private String timestamp;

        @Override
        public String toString() {
            return "Event{" +
                    "name=" + name +
                    ", gmv=" + gmv +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
