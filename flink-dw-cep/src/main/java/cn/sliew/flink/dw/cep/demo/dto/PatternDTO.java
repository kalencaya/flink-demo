package cn.sliew.flink.dw.cep.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class PatternDTO {

    private String key;
    private String name;
    private int level;
    private String combine;
    private RuleDTO rule;
    private List<PatternDTO> child;
}
