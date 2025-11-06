package cn.sliew.flink.dw.support.config;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class JdbcConfig implements Serializable {

    private String url;
    private String user;
    private String password;
    private String driver;

    private int flushMaxRows = 100;
    private int flushInterval = 2;
}
