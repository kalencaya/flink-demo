package cn.sliew.flink.dw.cep.condition;

import cn.sliew.flink.dw.cep.source.Event;
import org.apache.flink.cep.dynamic.condition.AviatorCondition;

public class StartCondition extends AviatorCondition<Event> {

    public StartCondition(String expression) {
        super(expression);
    }
}
