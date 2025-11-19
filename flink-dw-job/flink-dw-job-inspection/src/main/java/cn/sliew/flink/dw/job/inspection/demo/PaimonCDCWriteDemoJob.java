package cn.sliew.flink.dw.job.inspection.demo;

import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.CatalogLoader;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.flink.FlinkCatalogFactory;
import org.apache.paimon.flink.sink.FlinkSinkBuilder;
import org.apache.paimon.flink.sink.cdc.RichCdcRecord;
import org.apache.paimon.flink.sink.cdc.RichCdcSinkBuilder;
import org.apache.paimon.options.Options;
import org.apache.paimon.table.Table;

public class PaimonCDCWriteDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        final RichCdcRecord cdcRecord = RichCdcRecord.builder(org.apache.paimon.types.RowKind.INSERT)
                .field("name", org.apache.paimon.types.DataTypes.STRING(), "Alice")
                .field("age", org.apache.paimon.types.DataTypes.INT(), "12")
                .build();

        DataStream<Row> input =
                env.fromElements(
                                Row.ofKind(RowKind.INSERT, "Alice", 12),
                                Row.ofKind(RowKind.INSERT, "Bob", 5),
                                Row.ofKind(RowKind.UPDATE_BEFORE, "Alice", 12),
                                Row.ofKind(RowKind.UPDATE_AFTER, "Alice", 100))
                        .returns(
                                Types.ROW_NAMED(
                                        new String[] {"name", "age"}, Types.STRING, Types.INT));

        Options catalogOptions = new Options();
        catalogOptions.set("warehouse", "/Users/wangqi/Downloads/test");
        Catalog catalog = FlinkCatalogFactory.createPaimonCatalog(catalogOptions);
        Table table = catalog.getTable(Identifier.create("my_db", "T"));

        DataStreamSink<?> sink = new RichCdcSinkBuilder(table)
                .forRichCdcRecord(env.fromElements(cdcRecord))
                .identifier(Identifier.create("my_db", "T"))
                .catalogLoader(new CatalogLoader() {
                    @Override
                    public Catalog load() {
                        return FlinkCatalogFactory.createPaimonCatalog(catalogOptions);
                    }
                })
                .build();

        env.execute();
    }
}
