package cn.sliew.flink.dw.job.inspection.rule.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExpressionDTO {

    private String fieldName;
    private String operation;
    private List<Object> values;
    private RuleDTO child;

    public String toAviator() {
        return convert();
    }

    private String convert() {
        // todo 根据 operation 进行拼接
        if (operation.equals("==")) {
            return fieldName + " " + operation + " " + values.get(0);
        }
        return fieldName + " " + operation + " " + values.get(0);
    }
}
