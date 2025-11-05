package cn.sliew.flink.dw.cep.job;

import cn.sliew.flink.dw.support.util.KafkaUtil;
import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class InspectionJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);

        KafkaSource<String> source = KafkaUtil.getKafkaSource(parameterTool, "im-sync", "default");
        DataStreamSource<String> dataStreamSource = env.fromSource(source, WatermarkStrategy.noWatermarks(), "im-topic source");
        dataStreamSource.print();

        env.execute();
    }
}
