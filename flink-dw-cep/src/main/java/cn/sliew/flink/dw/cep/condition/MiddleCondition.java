package cn.sliew.flink.dw.cep.condition;

import cn.sliew.flink.dw.cep.source.Event;
import org.apache.flink.cep.pattern.conditions.SimpleCondition;

public class MiddleCondition extends SimpleCondition<Event> {

    @Override
    public boolean filter(Event value, Context<Event> ctx) throws Exception {
        return value.getName().contains("middle");
    }
}
