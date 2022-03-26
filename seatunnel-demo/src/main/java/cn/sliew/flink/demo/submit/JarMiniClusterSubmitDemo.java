package cn.sliew.flink.demo.submit;

import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.executors.LocalExecutor;
import org.apache.flink.client.program.MiniClusterClient;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.minicluster.MiniCluster;
import org.apache.flink.runtime.minicluster.MiniClusterConfiguration;

import java.io.File;
import java.net.URI;
import java.util.concurrent.ExecutionException;

public class JarMiniClusterSubmitDemo {

    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        MiniCluster cluster = newCluster(config);
        MiniClusterClient client = newClusterClient(cluster, config);

        String jarFilePath = "/Users/wangqi/Documents/software/flink/flink-1.13.6/examples/streaming/SocketWindowWordCount.jar";
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(jarFilePath))
                .setArguments("--port", "9000")
                .setEntryPointClassName("org.apache.flink.streaming.examples.socket.SocketWindowWordCount")
                .build();

        JobGraph jobGraph = PackagedProgramUtils.createJobGraph(program, config, 1, false);

        JobID jobId = client.submitJob(jobGraph).get();
        System.out.println(jobId);
    }

    private static MiniCluster newCluster(Configuration config) throws Exception {
//        config.setInteger(JobManagerOptions.PORT, JobManagerOptions.PORT.defaultValue());
//        config.setInteger(ConfigConstants.LOCAL_NUMBER_TASK_MANAGER, ConfigConstants.DEFAULT_LOCAL_NUMBER_TASK_MANAGER);
//        config.setInteger(TaskManagerOptions.NUM_TASK_SLOTS, TaskManagerOptions.NUM_TASK_SLOTS.defaultValue());
        MiniClusterConfiguration miniClusterConfig = new MiniClusterConfiguration.Builder()
                .setConfiguration(config)
                .build();
        MiniCluster cluster = new MiniCluster(miniClusterConfig);
        cluster.start();
        return cluster;
    }

    private static MiniClusterClient newClusterClient(MiniCluster cluster, Configuration config) throws ExecutionException, InterruptedException {
        URI address = cluster.getRestAddress().get();

        config.setString(JobManagerOptions.ADDRESS, address.getHost());
        config.setInteger(JobManagerOptions.PORT, address.getPort());
        config.setString(RestOptions.ADDRESS, address.getHost());
        config.setInteger(RestOptions.PORT, address.getPort());
        config.setString(DeploymentOptions.TARGET, LocalExecutor.NAME);

        return new MiniClusterClient(config, cluster);
    }
}
