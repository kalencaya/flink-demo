package wordcount

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object WordCountJob {

    def main(args: Array[String]): Unit = {
        val env = StreamExecutionEnvironment.getExecutionEnvironment
        val source = env.readTextFile("words.txt")
        source.flatMap(new WordFlatMapFunction())
            .map(string => Tuple2(string, 1))
            .keyBy(tuple => tuple._1)
            .sum(1)
            .print()
    }

}
