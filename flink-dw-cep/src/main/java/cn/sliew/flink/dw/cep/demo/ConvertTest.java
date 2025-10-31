package cn.sliew.flink.dw.cep.demo;

import cn.sliew.flink.dw.cep.demo.dto.*;
import cn.sliew.flink.dw.common.JacksonUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConvertTest {

    public static void main(String[] args) {
        PatternDTO patternDTO = buildSimplePattern();
        Object cepJson = ConverterUtil.toCepJson(patternDTO);

    }

    private static PatternDTO buildSimplePattern() {
        PatternDTO patternDTO1 = buildLevel1();
        PatternDTO patternDTO2 = buildLevel2();

        ChildPatternDTO childPatternDTO = new ChildPatternDTO();
        childPatternDTO.setKey("第二层");
        childPatternDTO.setName("第二层");
        childPatternDTO.setType("and");
        childPatternDTO.setPatterns(Collections.singletonList(patternDTO2));
        patternDTO1.setChild(childPatternDTO);

        System.out.println(JacksonUtil.toJsonString(patternDTO1));
        return patternDTO1;
    }

    private static PatternDTO buildLevel1() {
        PatternDTO patternDTO = new PatternDTO();
        patternDTO.setKey("第一层");
        patternDTO.setName("第一条用户发送的消息");
        patternDTO.setLevel(1);

        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setType("and");
        List<ExpressionDTO> expressionDTOS = new ArrayList<>();
        ExpressionDTO expressionDTO1 = new ExpressionDTO();
        expressionDTO1.setFieldName("消息发送人");
        expressionDTO1.setOperation("==");
        expressionDTO1.setValues(Arrays.asList("用户"));
        expressionDTOS.add(expressionDTO1);
        ExpressionDTO expressionDTO2 = new ExpressionDTO();
        expressionDTO2.setFieldName("消息类型");
        expressionDTO2.setOperation("in");
        expressionDTO2.setValues(Arrays.asList("文本", "图片"));
        expressionDTOS.add(expressionDTO2);
        ruleDTO.setExpressions(expressionDTOS);
        patternDTO.setRule(ruleDTO);
        return patternDTO;
    }

    private static PatternDTO buildLevel2() {
        PatternDTO patternDTO = new PatternDTO();
        patternDTO.setKey("第二层");
        patternDTO.setName("之后有客服发过言");
        patternDTO.setLevel(2);

        RuleDTO ruleDTO = new RuleDTO();
        ruleDTO.setType("and");
        List<ExpressionDTO> expressionDTOS = new ArrayList<>();
        ExpressionDTO expressionDTO1 = new ExpressionDTO();
        expressionDTO1.setFieldName("消息发送人");
        expressionDTO1.setOperation("==");
        expressionDTO1.setValues(Arrays.asList("客服"));
        expressionDTOS.add(expressionDTO1);
        ExpressionDTO expressionDTO2 = new ExpressionDTO();
        expressionDTO2.setFieldName("消息类型");
        expressionDTO2.setOperation("in");
        expressionDTO2.setValues(Arrays.asList("文本", "图片"));
        expressionDTOS.add(expressionDTO2);
        ruleDTO.setExpressions(expressionDTOS);
        patternDTO.setRule(ruleDTO);

        CombineDTO combineDTO = new CombineDTO();
        combineDTO.setDirection("after");
        combineDTO.setType("有");
        combineDTO.setQuantityType("句");
        combineDTO.setQuantity(5);
        combineDTO.setUnit("");
        patternDTO.setCombine(combineDTO);
        return patternDTO;
    }
}
