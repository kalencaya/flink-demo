package cn.sliew.flink.dw.job.inspection.rule.dto;

import lombok.Data;

import java.util.List;

@Data
public class RuleDTO {

    private String type;
    private List<ExpressionDTO> expressions;
}
