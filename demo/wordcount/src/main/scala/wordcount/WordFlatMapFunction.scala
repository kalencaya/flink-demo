package wordcount

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.util.Collector

class WordFlatMapFunction extends FlatMapFunction[String, String] {

    override def flatMap(word: String, collector: Collector[String]): Unit = {
        if (word.isBlank) {
            collector.collect(word)
        }

        val strings = word.split(" ")
        strings.foreach(collector.collect(_))
    }
}
