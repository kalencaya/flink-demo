package cn.sliew.flink.demo.submit;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.client.deployment.ClusterClientFactory;
import org.apache.flink.client.deployment.ClusterDeploymentException;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.DefaultClusterClientServiceLoader;
import org.apache.flink.client.program.*;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.DeploymentOptions;
import org.apache.flink.configuration.GlobalConfiguration;
import org.apache.flink.configuration.JobManagerOptions;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JarYarnPerJobSubmitDemo {

    private static final String FLINK_HOME = System.getenv("FLINK_HOME");
    private static final String FLINK_CONF_DIR = FLINK_HOME + "/conf";
    private static final String FLINK_PLUGINS_DIR = FLINK_HOME + "/plugins";
    private static final String FLINK_LIB_DIR = FLINK_HOME + "/lib";
    private static final String FLINK_DIST_JAR = FLINK_HOME + "/lib/flink-dist_2.11-1.13.6.jar";


    public static void main(String[] args) throws Exception {
        Configuration config = GlobalConfiguration.loadConfiguration(FLINK_CONF_DIR, new Configuration());
        ClusterClientFactory<ApplicationId> factory = newClientFactory(config);
        YarnClusterDescriptor clusterDescriptor = createClusterDescriptor(factory, config);
        ClusterSpecification clusterSpecification = createClusterSpecification();
        JobGraph jobGraph = createJobGraph(config);
        ClusterClient<ApplicationId> clusterClient = createClusterClient(clusterDescriptor, clusterSpecification, jobGraph);




    }

    private static ClusterClientFactory<ApplicationId> newClientFactory(Configuration config) {
        config.setString(JobManagerOptions.ADDRESS, YarnDeploymentTarget.PER_JOB.getName());
        config.setString(DeploymentOptions.TARGET, YarnDeploymentTarget.PER_JOB.getName());

        DefaultClusterClientServiceLoader serviceLoader = new DefaultClusterClientServiceLoader();
        return serviceLoader.getClusterClientFactory(config);
    }

    private static YarnClusterDescriptor createClusterDescriptor(ClusterClientFactory<ApplicationId> factory, Configuration config) throws MalformedURLException {
        YarnClusterDescriptor clusterDescriptor = (YarnClusterDescriptor) factory.createClusterDescriptor(config);
        boolean isRemoteJarPath =
                !CollectionUtil.isNullOrEmpty(config.get(YarnConfigOptions.PROVIDED_LIB_DIRS));
        List<File> shipFiles = new ArrayList<>();
        shipFiles.addAll(Arrays.asList(new File(FLINK_PLUGINS_DIR)));
        File[] jars = new File(FLINK_LIB_DIR).listFiles();
        if (jars != null) {
            for (File jar : jars) {
                if (jar.toURI().toURL().toString().contains("flink-dist")) {
                    clusterDescriptor.setLocalJarPath(new Path(jar.toURI().toURL().toString()));
                } else if (!isRemoteJarPath) {
                    shipFiles.add(jar);
                }
            }
        }
        clusterDescriptor.addShipFiles(shipFiles);
        return clusterDescriptor;
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
                                                                    ClusterSpecification clusterSpecification,
                                                                    JobGraph jobGraph) throws ClusterDeploymentException {

        ClusterClientProvider<ApplicationId> provider = clusterDescriptor.deployJobCluster(clusterSpecification, jobGraph, true);
        ClusterClient<ApplicationId> clusterClient = provider.getClusterClient();
        log.info("deploy per_job with appId: {}", clusterClient.getClusterId());
        return clusterClient;
    }
}
