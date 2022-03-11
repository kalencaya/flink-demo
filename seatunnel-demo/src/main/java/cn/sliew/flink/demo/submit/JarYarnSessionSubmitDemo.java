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
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
public class JarYarnSessionSubmitDemo {

    //    private static final String FLINK_HOME = System.getenv("FLINK_HOME");
    private static final String HADOOP_HOME = System.getenv("HADOOP_HOME");
    //    private static final String HADOOP_HOME = "/Users/wangqi/Documents/software/hadoop/hadoop-3.2.1";
    private static final String HADOOP_CONF_DIR = HADOOP_HOME + "/etc/hadoop";


    private static final String FLINK_HOME = "/Users/wangqi/Documents/software/flink/flink-1.13.6";
    private static final String FLINK_CONF_DIR = FLINK_HOME + "/conf";
    private static final String FLINK_PLUGINS_DIR = FLINK_HOME + "/plugins";
    private static final String FLINK_LIB_DIR = FLINK_HOME + "/lib";
    private static final String FLINK_EXAMPLES_DIR = FLINK_HOME + "/examples";
    private static final String FLINK_DIST_JAR = FLINK_HOME + "/lib/flink-dist_2.11-1.13.6.jar";

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
        config.setString(YarnConfigOptions.APPLICATION_ID, "application_1646981816129_0003");
        config.setString(DeploymentOptions.TARGET, YarnDeploymentTarget.SESSION.getName());

        DefaultClusterClientServiceLoader serviceLoader = new DefaultClusterClientServiceLoader();
        return serviceLoader.getClusterClientFactory(config);
    }


    private static ClusterSpecification createClusterSpecification() {
        return new ClusterSpecification.ClusterSpecificationBuilder()
                .createClusterSpecification();
    }

    private static JobGraph createJobGraph(Configuration config) throws ProgramInvocationException, UnknownHostException {
        String jarFilePath = FLINK_EXAMPLES_DIR + "/streaming/TopSpeedWindowing.jar";
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(jarFilePath))
//                .setArguments("--port", "9000", "--host", InetAddress.getLocalHost().getHostAddress())
                .setEntryPointClassName("org.apache.flink.streaming.examples.windowing.TopSpeedWindowing")
                .build();
        return PackagedProgramUtils.createJobGraph(program, config, 1, false);
    }

    private static ClusterClient<ApplicationId> createClusterClient(YarnClusterDescriptor clusterDescriptor,
                                                                    ClusterSpecification clusterSpecification) throws ClusterDeploymentException {

        // 创建 yarn session flink 集群功能。
        ClusterClientProvider<ApplicationId> provider = clusterDescriptor.deploySessionCluster(clusterSpecification);
        ClusterClient<ApplicationId> clusterClient = provider.getClusterClient();

        log.info("deploy session with appId: {}", clusterClient.getClusterId());
        return clusterClient;
    }
}
