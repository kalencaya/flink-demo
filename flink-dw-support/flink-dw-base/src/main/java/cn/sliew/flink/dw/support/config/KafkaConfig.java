package cn.sliew.flink.dw.support.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class KafkaConfig implements Serializable {

    private String servers;
}
