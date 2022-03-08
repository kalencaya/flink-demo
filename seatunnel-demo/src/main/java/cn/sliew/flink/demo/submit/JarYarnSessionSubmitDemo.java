package cn.sliew.flink.demo.submit;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterClientFactory;
import org.apache.flink.client.deployment.ClusterDeploymentException;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.DefaultClusterClientServiceLoader;
import org.apache.flink.client.program.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class JarYarnSessionSubmitDemo {

    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        ClusterClientFactory<ApplicationId> factory = newClientFactory(config);
        ApplicationId clusterId = factory.getClusterId(config);
        YarnClusterDescriptor clusterDescriptor = (YarnClusterDescriptor) factory.createClusterDescriptor(config);
        ClusterSpecification clusterSpecification = createClusterSpecification();
        JobGraph jobGraph = createJobGraph(config);
        ClusterClient<ApplicationId> clusterClient = createClusterClient(clusterDescriptor, clusterSpecification);
        JobID jobID = clusterClient.submitJob(jobGraph).get();
        System.out.println(jobID);
    }

    private static ClusterClientFactory<ApplicationId> newClientFactory(Configuration config) {
        config.setString(JobManagerOptions.ADDRESS, "localhost");
        config.setString(DeploymentOptions.TARGET, YarnDeploymentTarget.SESSION.getName());

        DefaultClusterClientServiceLoader serviceLoader = new DefaultClusterClientServiceLoader();
        return serviceLoader.getClusterClientFactory(config);
    }


    private static ClusterSpecification createClusterSpecification() {
        return new ClusterSpecification.ClusterSpecificationBuilder()
                .createClusterSpecification();
    }

    private static JobGraph createJobGraph(Configuration config) throws ProgramInvocationException, UnknownHostException {
        String jarFilePath = "/Users/wangqi/Documents/software/flink/flink-1.13.6/examples/streaming/SocketWindowWordCount.jar";
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(jarFilePath))
                .setArguments("--port", "9000", "--host", InetAddress.getLocalHost().getHostAddress())
                .setEntryPointClassName("org.apache.flink.streaming.examples.socket.SocketWindowWordCount")
                .build();
        return PackagedProgramUtils.createJobGraph(program, config, 1, false);
    }

    private static ClusterClient<ApplicationId> createClusterClient(YarnClusterDescriptor clusterDescriptor,
                                                                    ClusterSpecification clusterSpecification) throws ClusterDeploymentException {

        ClusterClientProvider<ApplicationId> provider = clusterDescriptor.deploySessionCluster(clusterSpecification);
        ClusterClient<ApplicationId> clusterClient = provider.getClusterClient();

        log.info("deploy session with appId: {}", clusterClient.getClusterId());
        return clusterClient;
    }
}
