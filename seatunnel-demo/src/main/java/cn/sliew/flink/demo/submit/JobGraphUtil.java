package cn.sliew.flink.demo.submit;

import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.client.program.ProgramInvocationException;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.jobgraph.JobGraph;

import java.io.File;

public enum JobGraphUtil {
    ;

    private static final String FLINK_HOME = System.getenv("FLINK_HOME");
    private static final String HADOOP_HOME = System.getenv("HADOOP_HOME");
//    private static final String HADOOP_HOME = "/Users/wangqi/Documents/software/hadoop/hadoop-3.2.1";
//    private static final String FLINK_HOME = "/Users/wangqi/Documents/software/flink/flink-1.13.6";

    private static final String FLINK_CONF_DIR = FLINK_HOME + "/conf";
    private static final String FLINK_PLUGINS_DIR = FLINK_HOME + "/plugins";
    private static final String FLINK_LIB_DIR = FLINK_HOME + "/lib";
    private static final String FLINK_EXAMPLES_DIR = FLINK_HOME + "/examples";
    private static final String FLINK_DIST_JAR = FLINK_HOME + "/lib/flink-dist_2.11-1.13.6.jar";

    private static final String HADOOP_CONF_DIR = HADOOP_HOME + "/etc/hadoop";

    public static final String JAR_FILE_PATH = FLINK_EXAMPLES_DIR + "/streaming/TopSpeedWindowing.jar";
    public static final String ENTRY_POINT_CLASS_NAME = "org.apache.flink.streaming.examples.windowing.TopSpeedWindowing";

    public static JobGraph createJobGraph(Configuration config) throws ProgramInvocationException {
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(JAR_FILE_PATH))
//                .setArguments("--port", "9000", "--host", InetAddress.getLocalHost().getHostAddress())
                .setEntryPointClassName(ENTRY_POINT_CLASS_NAME)
                .build();
        return PackagedProgramUtils.createJobGraph(program, config, 1, false);
    }
}
