package cn.sliew.flink.demo.submit;

import cn.sliew.flink.demo.submit.protocol.JarRunRequest;
import cn.sliew.flink.demo.submit.protocol.JarRunResponse;
import cn.sliew.flink.demo.submit.protocol.JarUploadResponse;
import cn.sliew.milky.common.util.JacksonUtil;
import org.apache.flink.api.common.JobID;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class JarStandaloneSubmitDemo02 {

    public static void main(String[] args) throws Exception {
        String webInterfaceURL = "http://localhost:8081";
        String jarFilePath = "/Users/wangqi/Documents/software/flink/flink-1.13.6/examples/streaming/SocketWindowWordCount.jar";
        JarUploadResponse jarUploadResponse = uploadJar(webInterfaceURL, new File(jarFilePath));
        String jarId = jarUploadResponse.getFilename().substring(jarUploadResponse.getFilename().lastIndexOf("/") + 1);
        JobID jobID = run(webInterfaceURL, jarId, "org.apache.flink.streaming.examples.socket.SocketWindowWordCount");
        System.out.println(jobID);
    }

    private static JarUploadResponse uploadJar(String webInterfaceURL, File jarFile) throws IOException {
        String response = Request.post(webInterfaceURL + "/jars/upload")
                .connectTimeout(Timeout.ofSeconds(3L))
                .responseTimeout(Timeout.ofSeconds(3L))
                .body(
                        MultipartEntityBuilder.create()
                                .addBinaryBody("jarfile", jarFile, ContentType.create("application/java-archive"), "SocketWindowWordCount.jar")
                                .build()
                ).execute().returnContent().asString(StandardCharsets.UTF_8);
        return JacksonUtil.parseJsonString(response, JarUploadResponse.class);
    }

    private static JobID run(String webInterfaceURL, String jarId, String entryClass) throws IOException {
        JarRunRequest jarRunRequest = new JarRunRequest();
        jarRunRequest.setEntryClass(entryClass);
        jarRunRequest.setProgramArgs("--port 9000");

        String response = Request.post(webInterfaceURL + "/jars/" + jarId + "/run")
                .connectTimeout(Timeout.ofSeconds(3L))
                .responseTimeout(Timeout.ofSeconds(60))
                .body(new StringEntity(JacksonUtil.toJsonString(jarRunRequest)))
                .execute().returnContent().asString(StandardCharsets.UTF_8);
        String jobID = JacksonUtil.parseJsonString(response, JarRunResponse.class).getJobID();
        return JobID.fromHexString(jobID);
    }
}
