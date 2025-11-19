package cn.sliew.flink.dw.job.inspection.demo;

import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.DataTypes;
import org.apache.flink.table.types.DataType;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.flink.FlinkCatalogFactory;
import org.apache.paimon.flink.sink.FlinkSinkBuilder;
import org.apache.paimon.options.Options;
import org.apache.paimon.schema.Schema;
import org.apache.paimon.table.Table;

public class PaimonWriteDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

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
        catalogOptions.set("metastore", "filesystem");
        Catalog catalog = FlinkCatalogFactory.createPaimonCatalog(catalogOptions);
        catalog.createDatabase("my_db", true);

        Schema schema = Schema.newBuilder()
                .column("name", org.apache.paimon.types.DataTypes.STRING())
                .column("age", org.apache.paimon.types.DataTypes.INT())
                .build();
        Identifier identifier = Identifier.create("my_db", "T");
        catalog.createTable(identifier, schema, true);

        Table table = catalog.getTable(identifier);

        DataType inputType =
                DataTypes.ROW(
                        DataTypes.FIELD("name", DataTypes.STRING()),
                        DataTypes.FIELD("age", DataTypes.INT()));
        FlinkSinkBuilder builder = new FlinkSinkBuilder(table).forRow(input, inputType);
        builder.build();

        env.execute();
    }
}
