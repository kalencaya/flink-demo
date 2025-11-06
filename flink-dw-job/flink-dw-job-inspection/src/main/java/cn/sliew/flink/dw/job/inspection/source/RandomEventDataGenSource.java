package cn.sliew.flink.dw.job.inspection.source;

import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.flink.api.connector.source.SourceReaderContext;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.connector.datagen.source.GeneratorFunction;

import java.util.List;

public class RandomEventDataGenSource implements GeneratorFunction<Long, Event> {

    private List<Tuple2<Integer, String>> users = Lists.newArrayList();

    @Override
    public void open(SourceReaderContext readerContext) throws Exception {
        for (int i = 0; i < 10; i++) {
            users.add(Tuple2.of(RandomUtils.nextInt(1, 1000_000_000), RandomStringUtils.randomAlphabetic(3, 6)));
        }
    }

    @Override
    public Event map(Long index) throws Exception {
        Tuple2<Integer, String> user = users.get(RandomUtils.nextInt(0, 10));
        return new Event(
                user.f0,
                user.f1,
                RandomUtils.nextInt(1, 11),
                RandomUtils.nextInt(0, 2),
                System.currentTimeMillis()
        );
    }
}
