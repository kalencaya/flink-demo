package cn.sliew.flink.dw.cep.job;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.dynamic.condition.AviatorCondition;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SimpleDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        DataStreamSource<Event> source = getSource(env);
        Pattern<Event, Event> pattern = Pattern.<Event>begin("start", AfterMatchSkipStrategy.skipPastLastEvent())
                .where(new AviatorCondition<>("action == 0"))
                .followedBy("end")
                .where(new AviatorCondition<>("action != 1"));

        SingleOutputStreamOperator<String> process = CEP.pattern(source, pattern).process(new PatternProcessFunction<Event, String>() {
            @Override
            public void processMatch(Map<String, List<Event>> match, Context context, Collector<String> out) throws Exception {
                StringBuilder sb = new StringBuilder();
                sb.append("A match for Pattern  is found. The event sequence: ");
                for (Map.Entry<String, List<Event>> entry : match.entrySet()) {
                    sb.append(entry.getKey()).append(": ").append(entry.getValue());
                }
                out.collect(sb.toString());
            }
        });

        process.print();

        env.execute();
    }

    private static DataStreamSource<Event> getSource(StreamExecutionEnvironment env) {
        return env.fromCollection(
                Arrays.asList(
                        new Event("ken", 1, 1, 0, 1662022777000L),
                        new Event("ken", 2, 1, 0, 1662022778000L),
                        new Event("ken", 3, 1, 1, 1662022779000L),
                        new Event("ken", 4, 1, 2, 1662022780000L),
                        new Event("ken", 5, 1, 1, 1662022780000L)
                )
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Event {
        private String name;
        private int id;
        private int productionId;
        private int action;
        private long timestamp;
    }
}
