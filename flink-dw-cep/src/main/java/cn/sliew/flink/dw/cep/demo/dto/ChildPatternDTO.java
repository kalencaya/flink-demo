package cn.sliew.flink.dw.cep.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChildPatternDTO {

    private String type;
    private List<PatternDTO> patterns;
}
