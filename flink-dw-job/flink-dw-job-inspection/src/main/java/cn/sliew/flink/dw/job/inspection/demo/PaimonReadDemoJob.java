package cn.sliew.flink.dw.job.inspection.demo;

import cn.sliew.flink.dw.job.inspection.util.PaimonUtil;
import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.flink.source.FlinkSourceBuilder;
import org.apache.paimon.table.Table;

public class PaimonReadDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        Catalog catalog = PaimonUtil.getCatalogLoader().load();
        Table table = catalog.getTable(PaimonUtil.getTableIdentifier());

        // table = table.copy(Collections.singletonMap("scan.file-creation-time-millis", "..."));

        FlinkSourceBuilder builder = new FlinkSourceBuilder(table).env(env);

        // builder.sourceBounded(true);
        // builder.projection(...);
        // builder.predicate(...);
        // builder.limit(...);
        // builder.sourceParallelism(...);

        DataStream<Row> dataStream = builder.buildForRow();

        // use this datastream
        dataStream.executeAndCollect().forEachRemaining(System.out::println);

        env.execute();
    }
}
