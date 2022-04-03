package connector.json;

import connector.csv.CsvJob;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class JsonJob {

    public static void main(String[] args) throws Exception {
        String rootDir = CsvJob.class.getResource("/").toURI().getPath();
        // 输出文件路径 运行后在 target/classes 路径下
        String outFilePath = rootDir+"/user.json";
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        DataStreamSource<User> source = env.fromElements(new User(), new User());
//        source.write

    }
}
