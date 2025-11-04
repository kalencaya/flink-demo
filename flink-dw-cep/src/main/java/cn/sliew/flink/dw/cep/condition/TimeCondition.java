package cn.sliew.flink.dw.cep.condition;

import cn.sliew.flink.dw.cep.job.TimeConditionDemoJob;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.cep.pattern.conditions.RichIterativeCondition;
import org.apache.flink.configuration.Configuration;

public class TimeCondition extends RichIterativeCondition<TimeConditionDemoJob.Event> {

    @Override
    public void open(Configuration parameters) throws Exception {
        ParameterTool parameterTool = (ParameterTool) getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
    }

    @Override
    public boolean filter(TimeConditionDemoJob.Event event, Context<TimeConditionDemoJob.Event> context) throws Exception {
        // 业务主键作为 key
        long currentTimestamp = context.timestamp(); // 事件时间，如果设置的是处理时间，这里设置的是 cep 算子的 ingesttime
        long currentProcessingTime = context.currentProcessingTime(); // 处理时间
        System.out.println("事件时间测试: id: " + event.getId()
                + ", event.timestamp: " + event.getTimestamp()
                + ", currentTimestamp: " + context.timestamp()
                + ", currentProcessingTime: " + context.currentProcessingTime());
        // 总是为 true
        return true;
    }

}
