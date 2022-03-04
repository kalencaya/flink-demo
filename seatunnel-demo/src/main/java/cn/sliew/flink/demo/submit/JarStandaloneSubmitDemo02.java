package cn.sliew.flink.demo.submit;

import cn.sliew.flink.demo.submit.protocol.JarUploadResponse;
import cn.sliew.milky.common.util.JacksonUtil;
import org.apache.flink.client.deployment.*;
import org.apache.flink.client.program.ClusterClient;
import org.apache.flink.configuration.Configuration;

import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JarStandaloneSubmitDemo02 {

    public static void main(String[] args) throws Exception {
        Configuration config = new Configuration();
        ClusterClientFactory<StandaloneClusterId> factory = newClientFactory(config);
        StandaloneClusterId clusterId = factory.getClusterId(config);
        StandaloneClusterDescriptor clusterDescriptor = (StandaloneClusterDescriptor) factory.createClusterDescriptor(config);

        ClusterClient<StandaloneClusterId> client = clusterDescriptor.retrieve(clusterId).getClusterClient();
        String webInterfaceURL = client.getWebInterfaceURL();






    }

    private static JarUploadResponse uploadJar(String webInterfaceURL, Configuration config, File jarFile) throws IOException {
        String response = Request.post(webInterfaceURL + "/jars/upload")
                .connectTimeout(Timeout.ofSeconds(3L))
                .responseTimeout(Timeout.ofSeconds(3L))
                .body(
                        MultipartEntityBuilder.create()
                                .addBinaryBody("jarfile", jarFile, ContentType.create("application/java-archive"), "")
                                .build()
                ).execute().returnContent().asString(StandardCharsets.UTF_8);
        return JacksonUtil.parseJsonString(response, JarUploadResponse.class);
    }




    private static ClusterClientFactory<StandaloneClusterId> newClientFactory(Configuration config) {
        DefaultClusterClientServiceLoader serviceLoader = new DefaultClusterClientServiceLoader();
        return serviceLoader.getClusterClientFactory(config);
    }
}
