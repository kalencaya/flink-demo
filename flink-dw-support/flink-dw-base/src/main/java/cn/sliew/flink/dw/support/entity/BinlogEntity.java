package cn.sliew.flink.dw.support.entity;

import lombok.Data;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;

@Data
public class BinlogEntity {

    private String table;
    private JsonNode data;
}
