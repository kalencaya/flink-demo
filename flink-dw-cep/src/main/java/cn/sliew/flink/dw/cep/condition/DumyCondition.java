package cn.sliew.flink.dw.cep.condition;

import cn.sliew.flink.dw.cep.source.Event;
import org.apache.flink.cep.dynamic.impl.json.spec.ClassConditionSpec;
import org.apache.flink.cep.dynamic.impl.json.spec.ConditionSpec;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

public class DumyCondition extends SimpleCondition<Event> {

    private static final ConditionSpec INSTANCE = new ClassConditionSpec(DumyCondition.class.getCanonicalName());

    @Override
    public boolean filter(Event event, Context<Event> ctx) throws Exception {
        return false;
    }

    public static ConditionSpec getInstance() {
        return INSTANCE;
    }
}
