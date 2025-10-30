package cn.sliew.flink.dw.cep.rule;

import lombok.Data;

import java.util.List;

@Data
public class FilterRule implements Rule {

    private String fieldName;
    private String operation;
    // 可能是 id in [1, 2, 3] 这种规则
    private List<Object> values;

    @Override
    public String toExpression() {
        return fieldName + " " + operation + " " + values;
    }
}
