package cn.sliew.flink.dw.support.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class RedisConfig implements Serializable {

    public static final String DEFAULT_INSTANCE = "default";

    private String host;
    private int port;
    private String passwd;
    private int database;
}
