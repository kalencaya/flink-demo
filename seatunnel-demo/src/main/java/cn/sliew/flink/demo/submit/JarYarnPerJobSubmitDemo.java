package cn.sliew.flink.demo.submit;

import org.apache.flink.client.deployment.ClusterClientFactory;
import org.apache.flink.client.deployment.ClusterDescriptor;
import org.apache.flink.client.deployment.DefaultClusterClientServiceLoader;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class JarYarnPerJobSubmitDemo {

    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        ClusterClientFactory<ApplicationId> factory = newClientFactory(config);
        ApplicationId clusterId = factory.getClusterId(config);
        YarnClusterDescriptor clusterDescriptor = (YarnClusterDescriptor) factory.createClusterDescriptor(config);
        clusterDescriptor.setLocalJarPath(new Path("/Users/wangqi/Documents/software/flink/flink-1.13.6/lib/flink-dist_2.11-1.13.6.jar"));
        clusterDescriptor.addShipFiles(Arrays.asList(new File("/Users/wangqi/Documents/software/flink/flink-1.13.6/plugins")));

        String jarFilePath = "/Users/wangqi/Documents/software/flink/flink-1.13.6/examples/streaming/SocketWindowWordCount.jar";
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(jarFilePath))
                .setArguments("--port", "9000")
                .setEntryPointClassName("org.apache.flink.streaming.examples.socket.SocketWindowWordCount")
                .build();

        String clusterDescription = clusterDescriptor.getClusterDescription();
        System.out.println(clusterDescription);


    }

    private static ClusterClientFactory<ApplicationId> newClientFactory(Configuration config) {

        config.setString(DeploymentOptions.TARGET, YarnDeploymentTarget.PER_JOB.getName());

        DefaultClusterClientServiceLoader serviceLoader = new DefaultClusterClientServiceLoader();
        return serviceLoader.getClusterClientFactory(config);
    }
}
