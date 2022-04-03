package wordcount

import org.apache.flink.api.common.{JobExecutionResult, RuntimeExecutionMode}
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.core.execution.{JobClient, JobListener}

object WordCountJob {

    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        env.setRuntimeMode(RuntimeExecutionMode.BATCH)
        env.registerJobListener(new JobListener() {
            override def onJobSubmitted(jobClient: JobClient, throwable: Throwable): Unit = {
                println(jobClient.getJobID + " 已提交")
            }

            override def onJobExecuted(jobExecutionResult: JobExecutionResult, throwable: Throwable): Unit = {
                println(jobExecutionResult.getJobID + " 已执行")
            }
        })

        val userDir = System.getProperty("user.dir")
        val separator = System.getProperty("file.separator");
        val wordsPath = userDir + separator + "demo/wordcount/src/main/resources"

        val source = env.readTextFile(wordsPath)
        source.flatMap(new WordFlatMapFunction())
            .map(string => Tuple2(string, 1))
            .keyBy(tuple => tuple._1)
            .sum(1)
            .print()

        env.execute()
    }

}
