package cn.sliew.flink.demo.submit;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterClientFactory;
import org.apache.flink.client.deployment.ClusterDeploymentException;
import org.apache.flink.client.deployment.ClusterSpecification;
import org.apache.flink.client.deployment.DefaultClusterClientServiceLoader;
import org.apache.flink.client.program.*;
import org.apache.flink.configuration.*;
import org.apache.flink.runtime.jobgraph.JobGraph;
import org.apache.flink.runtime.util.HadoopUtils;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.yarn.YarnClusterDescriptor;
import org.apache.flink.yarn.configuration.YarnConfigOptions;
import org.apache.flink.yarn.configuration.YarnDeploymentTarget;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.yarn.api.records.ApplicationId;

import java.io.File;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class JarYarnPerJobSubmitDemo {

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
        Configuration config = GlobalConfiguration.loadConfiguration(FLINK_CONF_DIR, new Configuration());
        ClusterClientFactory<ApplicationId> factory = newClientFactory(config);
        YarnClusterDescriptor clusterDescriptor = createClusterDescriptor(factory, config);
        ClusterSpecification clusterSpecification = createClusterSpecification();
        JobGraph jobGraph = createJobGraph(config);
        ClusterClient<ApplicationId> clusterClient = createClusterClient(clusterDescriptor, clusterSpecification, jobGraph);
        JobID jobID = clusterClient.submitJob(jobGraph).get();
        System.out.println(jobID);
    }

    /**
     * 需提供 hadoop 的配置文件，以便 flink 获取 hadoop 集群地址。
     * 当使用 flink on yarn 配置时，不需要配置 {@link JobManagerOptions#ADDRESS} 参数，
     * 配置 hadoop 配置文件，可以通过 {@link CoreOptions#FLINK_YARN_CONF_DIR} 和 {@link CoreOptions#FLINK_HADOOP_CONF_DIR}，
     * {@link CoreOptions#FLINK_YARN_CONF_DIR} 拥有更高的优先级，当二者都未配置时，flink 会尝试从 HADOOP_HOME 环境变量
     * 获取 hadoop 配置。
     * {@link CoreOptions#FLINK_YARN_CONF_DIR} 和 {@link CoreOptions#FLINK_HADOOP_CONF_DIR} 只支持环境变量形式设置，
     * 设置两个参数的目的仅仅是为了文档的自动生成.
     *
     * @see HadoopUtils#getHadoopConfiguration(Configuration)
     */
    private static ClusterClientFactory<ApplicationId> newClientFactory(Configuration config) {
//        config.setString(JobManagerOptions.ADDRESS, "localhost");
//        config.setString(CoreOptions.FLINK_YARN_CONF_DIR, HADOOP_CONF_DIR);
//        config.setString(CoreOptions.FLINK_HADOOP_CONF_DIR, HADOOP_CONF_DIR);
        config.setString(DeploymentOptions.TARGET, YarnDeploymentTarget.PER_JOB.getName());

        DefaultClusterClientServiceLoader serviceLoader = new DefaultClusterClientServiceLoader();
        return serviceLoader.getClusterClientFactory(config);
    }

    /**
     * 也可以通过 {@link YarnConfigOptions#FLINK_DIST_JAR} 配置 flink-dist-xxx.jar
     * {@link YarnConfigOptions#SHIP_FILES} 配置 ship jars.
     */
    private static YarnClusterDescriptor createClusterDescriptor(ClusterClientFactory<ApplicationId> factory, Configuration config) throws MalformedURLException {
        YarnClusterDescriptor clusterDescriptor = (YarnClusterDescriptor) factory.createClusterDescriptor(config);
        boolean isRemoteJarPath =
                !CollectionUtil.isNullOrEmpty(config.get(YarnConfigOptions.PROVIDED_LIB_DIRS));
        List<File> shipFiles = new ArrayList<>();
        File[] plugins = new File(FLINK_PLUGINS_DIR).listFiles();
        if (plugins != null) {
            for (File plugin : plugins) {
                if (plugin.isDirectory() == false) {
                    continue;
                }
                if (!isRemoteJarPath) {
                    shipFiles.addAll(Arrays.asList(plugin.listFiles()));
                }
            }
        }
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
//        shipFiles.forEach(file -> System.out.println(file.getAbsolutePath()));
        clusterDescriptor.addShipFiles(shipFiles);
        return clusterDescriptor;
    }

    private static ClusterSpecification createClusterSpecification() {
        return new ClusterSpecification.ClusterSpecificationBuilder()
                .createClusterSpecification();
    }

    private static JobGraph createJobGraph(Configuration config) throws ProgramInvocationException, UnknownHostException {
//        String jarFilePath = "/Users/wangqi/Documents/software/flink/flink-1.13.6/examples/streaming/SocketWindowWordCount.jar";
        String jarFilePath = FLINK_EXAMPLES_DIR + "/streaming/TopSpeedWindowing.jar";
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(jarFilePath))
//                .setArguments("--port", "9000", "--host", InetAddress.getLocalHost().getHostAddress())
                .setEntryPointClassName("org.apache.flink.streaming.examples.windowing.TopSpeedWindowing")
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
