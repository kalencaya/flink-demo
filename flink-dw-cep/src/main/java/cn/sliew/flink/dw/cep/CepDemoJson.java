package cn.sliew.flink.dw.cep;

import cn.sliew.flink.dw.cep.condition.EndCondition;
import cn.sliew.flink.dw.cep.condition.StartCondition;
import cn.sliew.flink.dw.cep.source.Event;
import org.apache.flink.cep.dynamic.impl.json.util.CepJsonUtils;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;

public class CepDemoJson {

    public static void main(String[] args) throws Exception {
        Pattern<Event, Event> pattern =
                Pattern.<Event>begin("start", AfterMatchSkipStrategy.skipPastLastEvent())
                        .where(new StartCondition("action == 0 and "))
                        .timesOrMore(3)
                        .followedBy("end")
                        .where(new EndCondition());

        // show how to print test pattern in json format
        System.out.println(CepJsonUtils.convertPatternToJSONString(pattern));
    }
}
