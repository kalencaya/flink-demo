package cn.sliew.flink.dw.job.inspection.demo;

import cn.sliew.flink.dw.job.inspection.util.PaimonUtil;
import cn.sliew.flink.dw.support.util.ParameterToolUtil;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSink;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;
import org.apache.flink.types.RowKind;
import org.apache.paimon.catalog.Catalog;
import org.apache.paimon.catalog.CatalogLoader;
import org.apache.paimon.catalog.Identifier;
import org.apache.paimon.flink.sink.cdc.RichCdcRecord;
import org.apache.paimon.flink.sink.cdc.RichCdcSinkBuilder;
import org.apache.paimon.table.Table;

public class PaimonCDCWriteDemoJob {

    public static void main(String[] args) throws Exception {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        // 读取参数
        ParameterTool parameterTool = ParameterToolUtil.createParameterTool(args);
        env.getConfig().setGlobalJobParameters(parameterTool);
        env.setParallelism(1);

        RichCdcRecord cdcRecord = RichCdcRecord.builder(org.apache.paimon.types.RowKind.INSERT)
                .field("name", org.apache.paimon.types.DataTypes.STRING(), "Dog")
                .field("age", org.apache.paimon.types.DataTypes.INT(), "100")
                .build();

        CatalogLoader catalogLoader = PaimonUtil.getCatalogLoader();
        Catalog catalog = catalogLoader.load();

        Identifier tableIdentifier = PaimonUtil.getTableIdentifier();
        Table table = catalog.getTable(tableIdentifier);

        DataStreamSink<?> sink = new RichCdcSinkBuilder(table)
                .forRichCdcRecord(env.fromElements(cdcRecord))
                .identifier(tableIdentifier)
                .catalogLoader(catalogLoader)
                .build();

        env.execute();
    }
}
