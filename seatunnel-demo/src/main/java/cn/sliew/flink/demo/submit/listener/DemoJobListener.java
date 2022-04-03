package cn.sliew.flink.demo.submit.listener;

import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.core.execution.JobListener;

import javax.annotation.Nullable;

public class DemoJobListener implements JobListener {

    @Override
    public void onJobSubmitted(@Nullable JobClient jobClient, @Nullable Throwable throwable) {
        System.out.println(jobClient.getJobID() + " 已提交");
    }

    @Override
    public void onJobExecuted(@Nullable JobExecutionResult jobExecutionResult, @Nullable Throwable throwable) {
        System.out.println(jobExecutionResult.getJobID() + " 已执行");
    }
}
