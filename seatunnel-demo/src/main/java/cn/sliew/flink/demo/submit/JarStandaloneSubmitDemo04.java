package cn.sliew.flink.demo.submit;

import org.apache.flink.api.common.JobID;
import org.apache.flink.client.deployment.ClusterClientFactory;
import org.apache.flink.client.deployment.DefaultClusterClientServiceLoader;
import org.apache.flink.client.deployment.StandaloneClusterDescriptor;
import org.apache.flink.client.deployment.StandaloneClusterId;
import org.apache.flink.client.deployment.executors.RemoteExecutor;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.client.program.PackagedProgram;
import org.apache.flink.client.program.PackagedProgramUtils;
import org.apache.flink.configuration.*;
import org.apache.flink.runtime.jobgraph.JobGraph;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

public class JarStandaloneSubmitDemo04 {

    private static String SEATUNNEL_HOME = "/Users/wangqi/Documents/software/seatunnel/apache-seatunnel-incubating-2.1.3-SNAPSHOT/";

    private static String CORE = SEATUNNEL_HOME + "lib/seatunnel-flink-starter.jar";
    private static String FAKE_CONNECTOR = SEATUNNEL_HOME + "connectors/seatunnel/connector-fake-2.1.3-SNAPSHOT.jar";
    private static String CONSOLE_CONNECTOR = SEATUNNEL_HOME + "connectors/seatunnel/connector-console-2.1.3-SNAPSHOT.jar";
    private static String JDBC_CONNECTOR = SEATUNNEL_HOME + "connectors/seatunnel/connector-jdbc-2.1.3-SNAPSHOT.jar";
    private static String CONF_FILE = SEATUNNEL_HOME + "config/fake_to_console.conf";

    public static void main(String[] args) throws Exception {
        new File(FAKE_CONNECTOR).toURL();
        URL fake = new File(FAKE_CONNECTOR).toURL();
        URL jdbc = new File(JDBC_CONNECTOR).toURL();
        URL console = new File(CONSOLE_CONNECTOR).toURL();
        List<URL> jobJars = Arrays.asList(fake, console);
        PackagedProgram program = PackagedProgram.newBuilder()
                .setJarFile(new File(CORE))
                .setArguments("--config", CONF_FILE)
                .setEntryPointClassName("org.apache.seatunnel.core.starter.flink.SeatunnelFlink")
                .setUserClassPaths(jobJars)
                .build();

        Configuration config = new Configuration();
        ConfigUtils.encodeCollectionToConfig(config, PipelineOptions.JARS, jobJars, Object::toString);
        ClusterClientFactory<StandaloneClusterId> factory = newClientFactory(config);
        StandaloneClusterId clusterId = factory.getClusterId(config);
        StandaloneClusterDescriptor clusterDescriptor = (StandaloneClusterDescriptor) factory.createClusterDescriptor(config);
        ClusterClient<StandaloneClusterId> client = clusterDescriptor.retrieve(clusterId).getClusterClient();

        JobGraph jobGraph = PackagedProgramUtils.createJobGraph(program, config, 1, true);
        JobID jobId = client.submitJob(jobGraph).get();
        System.out.println(jobId);
    }

    /**
     * Standalone 模式下可以使用 jobmanager 的地址或者使用 rest 地址。
     * 对于 yarn session 和 native kubernetes session 模式下，jobmanager 的地址由 yarn 或 native kubernetes 下处理，
     * 推荐使用 rest 地址。
     * todo jobmanager 地址 和 webInterfaceUrl 的优先级问题？
     */
    private static ClusterClientFactory<StandaloneClusterId> newClientFactory(Configuration config) {
        config.setString(JobManagerOptions.ADDRESS, "localhost");
        config.setInteger(JobManagerOptions.PORT, 6123);
        config.setString(DeploymentOptions.TARGET, RemoteExecutor.NAME);

        DefaultClusterClientServiceLoader serviceLoader = new DefaultClusterClientServiceLoader();
        return serviceLoader.getClusterClientFactory(config);
    }
}
