package cn.sliew.flink.dw.cep;

import cn.sliew.flink.dw.cep.condition.EndCondition;
import cn.sliew.flink.dw.cep.condition.StartCondition;
import cn.sliew.flink.dw.cep.dynamic.JDBCPeriodicPatternProcessorDiscovererFactory;
import cn.sliew.flink.dw.cep.source.Event;
import cn.sliew.flink.dw.cep.source.LoopEventDataGenSource;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.connector.source.util.ratelimit.RateLimiterStrategy;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.CEP;
import org.apache.flink.cep.TimeBehaviour;
import org.apache.flink.cep.dynamic.impl.json.util.CepJsonUtils;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.connector.datagen.source.DataGeneratorSource;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.KeyedStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class CepDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        DataGeneratorSource<Event> source = getEventSource();
        DataStreamSource<Event> stream =
                env.fromSource(source,
                        WatermarkStrategy.<Event>forMonotonousTimestamps()
                                .withTimestampAssigner((event, ts) -> event.getEventTime()),
                        "Event GeneratorSource");

        KeyedStream<Event, Tuple2<Integer, Integer>> keyedStream = stream.keyBy(new KeySelector<Event, Tuple2<Integer, Integer>>() {
            @Override
            public Tuple2<Integer, Integer> getKey(Event event) throws Exception {
                return Tuple2.of(event.getId(), event.getProductionId());
            }
        });

        Pattern<Event, Event> pattern =
                Pattern.<Event>begin("start", AfterMatchSkipStrategy.skipPastLastEvent())
                        .where(new StartCondition("action == 0"))
                        .timesOrMore(3)
                        .followedBy("end")
                        .where(new EndCondition());

        // show how to print test pattern in json format
        printTestPattern(pattern);

        // Dynamic CEP patterns
//        SingleOutputStreamOperator<String> output =
//                CEP.dynamicPatterns(
//                        keyedStream,
//                        new JDBCPeriodicPatternProcessorDiscovererFactory<>(
//                                parameterTool.get("cep.rule.jdbc.url"),
//                                "com.mysql.cj.jdbc.Driver",
//                                parameterTool.get("cep.rule.tableName"),
//                                null,
//                                parameterTool.getLong("cep.rule.jdbcIntervalMs")),
//                        TimeBehaviour.EventTime,
//                        TypeInformation.of(new TypeHint<String>() {
//                        }));
//
//        output.print();

        // 触发执行
        env.execute("cep-demo-job");
    }

    private static DataGeneratorSource<Event> getEventSource() {
        return new DataGeneratorSource<>(new LoopEventDataGenSource(), 1000, RateLimiterStrategy.perSecond(1), TypeInformation.of(Event.class));
    }

    public static void printTestPattern(Pattern<?, ?> pattern) throws JsonProcessingException {
        System.out.println(CepJsonUtils.convertPatternToJSONString(pattern));
    }
}
