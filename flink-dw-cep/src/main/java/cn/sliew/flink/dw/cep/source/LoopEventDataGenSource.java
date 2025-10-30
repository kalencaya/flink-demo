package cn.sliew.flink.dw.cep.source;

import org.apache.flink.connector.datagen.source.GeneratorFunction;

import java.util.Arrays;
import java.util.List;

public class LoopEventDataGenSource implements GeneratorFunction<Long, Event> {

    private List<Event> initialEvents = Arrays.asList(
            new Event(1, "ken", 0, 1, 1662022777000L),
            new Event(1, "ken", 0, 1, 1662022778000L),
            new Event(1, "ken", 0, 1, 1662022779000L),
            new Event(1, "ken", 0, 1, 1662022780000L),
            new Event(1, "ken", 0, 1, 1662022780000L),
            new Event(2, "Tom", 0, 1, 1739584800000L),
            new Event(2, "Tom", 0, 1, 1739585400000L),
            new Event(2, "Tom", 0, 1, 1739585700000L),
            new Event(2, "Tom", 0, 1, 1739586000000L),
            new Event(3, "Ali", 0, 1, 1739586600000L),
            new Event(3, "Ali", 0, 1, 1739588400000L),
            new Event(3, "Ali", 0, 1, 1739589000000L),
            new Event(3, "Ali", 0, 1, 1739590200000L)
    );

    @Override
    public Event map(Long index) throws Exception {
        return initialEvents.get(index.intValue() % initialEvents.size());
    }
}
