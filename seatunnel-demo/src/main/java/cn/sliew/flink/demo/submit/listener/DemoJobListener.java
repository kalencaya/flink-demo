package cn.sliew.flink.demo.submit.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.JobExecutionResult;
import org.apache.flink.core.execution.JobClient;
import org.apache.flink.core.execution.JobListener;

import javax.annotation.Nullable;

@Slf4j
public class DemoJobListener implements JobListener {

    @Override
    public void onJobSubmitted(@Nullable JobClient jobClient, @Nullable Throwable throwable) {
        System.out.println(jobClient.getJobID() + " 已提交");
        log.info("{} 已提交", jobClient.getJobID());
    }

    @Override
    public void onJobExecuted(@Nullable JobExecutionResult jobExecutionResult, @Nullable Throwable throwable) {
        System.out.println(jobExecutionResult.getJobID() + " 已执行");
        log.info("{} 已执行", jobExecutionResult.getJobID());
    }
}
