package cn.sliew.flink.dw.cep;

import org.apache.flink.cep.dynamic.condition.AviatorCondition;
import org.apache.flink.cep.dynamic.impl.json.util.CepJsonUtils;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.GroupPattern;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

public class CepDemoJson {

    public static void main(String[] args) throws Exception {
        Pattern<String, String> patternLevel1 = Pattern.<String>begin("第一层", AfterMatchSkipStrategy.noSkip())
                .where(new AviatorCondition<>("消息发送人 == 用户"))
                .where(new AviatorCondition<>("消息类型 == 文本or图片"));

        Pattern<String, String> patternLevel2 = Pattern.<String>begin("第二层", AfterMatchSkipStrategy.noSkip())
                .where(new AviatorCondition<>("消息发送人 == 客服"))
                .where(new AviatorCondition<>("消息类型 == 文本or图片"));


        GroupPattern<String, String> finalPattern = patternLevel1
                .followedBy(countPattern())
                .next(patternLevel2);

        System.out.println(CepJsonUtils.convertPatternToJSONString(finalPattern));
    }

    private static <T> Pattern<T, T> countPattern() {
        Pattern<T, T> dumyPattern = Pattern.<T>begin("之后 n 句", AfterMatchSkipStrategy.noSkip())
                .where(new SimpleCondition<T>() {
                    @Override
                    public boolean filter(T s) throws Exception {
                        return true;
                    }
                })
                .times(1, 4).optional();
        return dumyPattern;
    }
}
