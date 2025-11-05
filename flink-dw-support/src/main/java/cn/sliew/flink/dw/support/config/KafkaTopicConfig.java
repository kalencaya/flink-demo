package cn.sliew.flink.dw.support.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KafkaTopicConfig implements Serializable {

    private String servers;
    private String topic;
    private String gid;
    private String scanStartupMode;
}
