package cn.sliew.flink.dw.cep.demo;

import cn.sliew.flink.dw.cep.condition.DumyCondition;
import cn.sliew.flink.dw.cep.demo.dto.ExpressionDTO;
import cn.sliew.flink.dw.cep.demo.dto.PatternDTO;
import cn.sliew.flink.dw.cep.demo.dto.RuleDTO;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.cep.dynamic.impl.json.spec.AviatorConditionSpec;
import org.apache.flink.cep.dynamic.impl.json.spec.ConditionSpec;
import org.apache.flink.cep.dynamic.impl.json.spec.NodeSpec;
import org.apache.flink.shaded.guava30.com.google.common.graph.GraphBuilder;
import org.apache.flink.shaded.guava30.com.google.common.graph.MutableGraph;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum ConverterUtil {
    ;

    public static Object toCepJson(PatternDTO dto) {
        // 将层级的操作，变成 dag。这一步其实就是 a -> b -> c


        Map<String, NodeSpec> cache = new HashMap<>();
        MutableGraph<NodeSpec> dag = GraphBuilder.directed().build();

        convert2Node(dag, cache, null, dto);


        return null;
    }

    private static void convert2Node(MutableGraph<NodeSpec> dag, Map<String, NodeSpec> cache, PatternDTO parent, PatternDTO dto) {
        NodeSpec spec = convert2Node(dto);
        dag.addNode(spec);
        if (parent != null) {
            dag.putEdge(cache.get(parent.getKey()), spec);
        }
    }

    private static NodeSpec convert2Node(PatternDTO dto) {
        String name = dto.getKey();
        ConditionSpec conditionSpec = null;
        RuleDTO ruleDTO = dto.getRule();
        org.apache.commons.collections.CollectionUtils
        if (CollectionUtils.isEmpty(ruleDTO.getExpressions())) {
            conditionSpec = DumyCondition.getInstance();
        } else {
            String expression = ruleDTO.getExpressions().stream()
                    .map(ExpressionDTO::toAviator)
                    .collect(Collectors.joining(" " + ruleDTO.getType() + " "));
            conditionSpec = new AviatorConditionSpec(expression);
        }
        // 检测是之前，还是之后
        // 测试是 n 句，还是 n 秒。n 句需翻译成
        return new NodeSpec(name, null, conditionSpec);
    }
}
