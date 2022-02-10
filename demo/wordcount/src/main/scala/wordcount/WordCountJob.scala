package wordcount

import java.io.File

import org.apache.flink.api.common.RuntimeExecutionMode
import org.apache.flink.api.scala._
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object WordCountJob {

    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        env.setParallelism(1)
        env.setRuntimeMode(RuntimeExecutionMode.BATCH)

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
