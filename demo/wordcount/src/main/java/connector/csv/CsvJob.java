package connector.csv;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.core.fs.FileSystem;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Date;

public class CsvJob {

    public static void main(String[] args) throws Exception {
        String rootDir = CsvJob.class.getResource("/").toURI().getPath();
        // 输出文件路径 运行后在 target/classes 路径下
        String outFilePath = rootDir+"/order.csv";

        StreamExecutionEnvironment environment = StreamExecutionEnvironment.getExecutionEnvironment();
        environment.setParallelism(1);
        Order order1 = new Order(1L, 1L, 0.1, "wangqi", new Date());
        Order order2 = new Order(2L, 2L, 0.2, "wangqi", new Date());
        DataStreamSource<Order> source = environment.fromElements(order1, order2);

        // writeAsCsv 只能处理 Tuple 类型的数据 这里做一下转换，也可以输出为文本
        DataStream<Tuple5<Long, Long, Double, String, Date>> tuple3 = source.map(
                new MapFunction<Order, Tuple5<Long, Long, Double, String, Date>>() {
                    @Override
                    public Tuple5<Long, Long, Double, String, Date> map(Order order) throws Exception {
                        return new Tuple5(order.getOrderId(), order.getPrice(), order.getPayAmount(), order.getBuyerName(), order.getCreateTime());
                    }
                });

        // 输出到csv文件
        tuple3.writeAsCsv(outFilePath, FileSystem.WriteMode.OVERWRITE);

        // 触发执行
        environment.execute();
    }
}
