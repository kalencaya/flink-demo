package cn.sliew.flink.dw.cep.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class RuleDTO {

    private String type;
    private List<ExpressionDTO> expressions;
}
