package cn.sliew.flink.demo.submit;

import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.StandaloneClusterId;
import org.apache.flink.client.deployment.application.ApplicationRunner;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.client.program.rest.RestClusterClient;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.configuration.RestOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;

import java.io.File;

public class JarSubmitDemo {

    public static void main(String[] args) throws Exception {
        ApplicationRunner runner;

        String jarFilePath = "/opt/flink/examples/streaming/SocketWindowWordCount.jar";
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(jarFilePath))
                .setArguments("--port", "9000")
                .setEntryPointClassName("")
                .build();

        Configuration config = new Configuration();
        config.setString(JobManagerOptions.ADDRESS, "127.0.0.1");
        config.setInteger(RestOptions.PORT, 8081);
        config.setInteger(RestOptions.RETRY_MAX_ATTEMPTS, 3);
        RestClusterClient<StandaloneClusterId> client = new RestClusterClient<>(config, StandaloneClusterId.getInstance());

        JobGraph jobGraph = PackagedProgramUtils.createJobGraph(program, config, 1, false);

        JobID jobId = client.submitJob(jobGraph).get();
    }
}
