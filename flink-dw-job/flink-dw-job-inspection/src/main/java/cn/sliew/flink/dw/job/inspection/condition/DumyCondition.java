package cn.sliew.flink.dw.job.inspection.condition;

import org.apache.flink.cep.dynamic.impl.json.spec.ClassConditionSpec;
import org.apache.flink.cep.dynamic.impl.json.spec.ConditionSpec;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

public class DumyCondition<T> extends SimpleCondition<T> {

    private static final ConditionSpec SPEC_INSTANCE = new ClassConditionSpec(DumyCondition.class.getCanonicalName());
    private static final DumyCondition INSTANCE = new DumyCondition();

    @Override
    public boolean filter(T event) throws Exception {
        return true;
    }

    public static ConditionSpec getSpecInstance() {
        return SPEC_INSTANCE;
    }

    public static <T> DumyCondition<T> getInstance() {
        return INSTANCE;
    }
}
