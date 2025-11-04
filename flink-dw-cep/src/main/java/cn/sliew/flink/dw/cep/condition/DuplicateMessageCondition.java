package cn.sliew.flink.dw.cep.condition;

import cn.sliew.flink.dw.cep.source.Event;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.pattern.conditions.RichIterativeCondition;
import org.apache.flink.configuration.Configuration;

public class DuplicateMessageCondition extends RichIterativeCondition<Event> {

    // 声明状态

    @Override
    public void open(Configuration parameters) throws Exception {
        super.open(parameters);
        ParameterTool parameterTool = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        // 获取状态
    }

    @Override
    public boolean filter(Event event, Context<Event> context) throws Exception {
        return false;
    }
}
