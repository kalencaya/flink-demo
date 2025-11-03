package cn.sliew.flink.dw.cep.demo;

import cn.sliew.flink.dw.cep.demo.dto.CombineDTO;
import cn.sliew.flink.dw.cep.demo.dto.ExpressionDTO;
import cn.sliew.flink.dw.cep.demo.dto.PatternDTO;
import cn.sliew.flink.dw.cep.demo.dto.RuleDTO;
import org.apache.flink.cep.dynamic.impl.json.util.CepJsonUtils;
import org.apache.flink.cep.pattern.Pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConvertTest {

    public static void main(String[] args) throws Exception {
        PatternDTO patternDTO = buildSimplePattern();
        Object cepJson = ConverterUtil.toCepJson(patternDTO);
        Pattern pattern = PatternUtil.convertPattern(patternDTO);
        System.out.println(CepJsonUtils.convertPatternToJSONString(pattern));
    }

    private static PatternDTO buildSimplePattern() {
        PatternDTO patternDTO1 = buildLevel1();
        PatternDTO patternDTO2 = buildLevel2();

        patternDTO1.setChild(patternDTO2);
        CombineDTO combineDTO = new CombineDTO();
        combineDTO.setDirection("after");
        combineDTO.setType("有");
        combineDTO.setQuantityType("句");
        combineDTO.setQuantity(5);
        combineDTO.setUnit("");
        patternDTO1.setCombine(combineDTO);

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
        expressionDTO2.setOperation("==");
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
        expressionDTO2.setOperation("==");
        expressionDTO2.setValues(Arrays.asList("文本", "图片"));
        expressionDTOS.add(expressionDTO2);
        ruleDTO.setExpressions(expressionDTOS);
        patternDTO.setRule(ruleDTO);


        return patternDTO;
    }
}
