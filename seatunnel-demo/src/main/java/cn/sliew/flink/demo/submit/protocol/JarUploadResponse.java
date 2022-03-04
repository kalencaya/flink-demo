package cn.sliew.flink.demo.submit.protocol;

import lombok.Getter;
import lombok.Setter;

/**
 * https://nightlies.apache.org/flink/flink-docs-release-1.14/docs/ops/rest_api/
 */
@Getter
@Setter
public class JarUploadResponse {

    private String filename;

    private String status;
}
