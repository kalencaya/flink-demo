package cn.sliew.flink.dw.cep.demo;

import cn.sliew.flink.dw.cep.demo.dto.ExpressionDTO;
import cn.sliew.flink.dw.cep.demo.dto.RuleDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.cep.dynamic.condition.AviatorCondition;
import org.apache.flink.cep.pattern.conditions.BooleanConditions;
import org.apache.flink.cep.pattern.conditions.IterativeCondition;
import org.apache.flink.cep.pattern.conditions.RichAndCondition;
import org.apache.flink.cep.pattern.conditions.RichOrCondition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public enum ConditionUtil {
    ;

    public static <T> IterativeCondition<T> convert(RuleDTO ruleDTO) {
        if (CollectionUtils.isEmpty(ruleDTO.getExpressions())) {
            return BooleanConditions.trueFunction();
        }
        // and or 只支持 2 个
        List<IterativeCondition<T>> conditions = ruleDTO.getExpressions().stream()
                .map(expressionDTO -> (IterativeCondition<T>) convertExpression(expressionDTO))
                .toList();
        // 每 2 个拼成一个 RichAndCondition
        return compositeByAndOr(conditions, ruleDTO.getType());
    }

    private static <T> IterativeCondition<T> convertExpression(ExpressionDTO expressionDTO) {
        IterativeCondition<T> expressionCondition = null;
        if (expressionDTO.getOperation().equals("连续重复")) {
            // todo 定制连续重复函数
            expressionCondition = null;
            // todo 按需定制其他函数
        } else {
            expressionCondition = new AviatorCondition<>(expressionDTO.toAviator());
        }
        if (Objects.nonNull(expressionDTO.getChild())) {
            IterativeCondition<T> childRuleCondition = convert(expressionDTO.getChild());
            return new RichAndCondition<>(expressionCondition, childRuleCondition);
        } else {
            return expressionCondition;
        }
    }

    private static <T> IterativeCondition<T> compositeByAndOr(List<IterativeCondition<T>> conditions, String type) {
        if (CollectionUtils.size(conditions) == 1) {
            return conditions.get(0);
        }
        List<IterativeCondition<T>> compositeConditions = new ArrayList<>();
        // 每次跳 2 个
        for (int i = 0; i < conditions.size(); i += 2) {
            IterativeCondition<T> left = conditions.get(i);
            IterativeCondition<T> right = null;
            if (i + 1 < conditions.size()) {
                right = conditions.get(i + 1);
                // todo and or 枚举
                if (type.equals("and")) {
                    compositeConditions.add(new RichAndCondition<>(left, right));
                } else if (type.equals("or")) {
                    compositeConditions.add(new RichOrCondition<>(left, right));
                }
            } else {
                compositeConditions.add(left);
            }
        }
        return compositeByAndOr(compositeConditions, type);
    }
}
