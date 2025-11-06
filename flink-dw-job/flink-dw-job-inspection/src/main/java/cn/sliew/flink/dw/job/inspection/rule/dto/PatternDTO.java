package cn.sliew.flink.dw.job.inspection.rule.dto;

import lombok.Data;

@Data
public class PatternDTO {

    private String key;
    private String name;
    private int level;
    private CombineDTO combine;
    private RuleDTO rule;
    private PatternDTO child;
}
