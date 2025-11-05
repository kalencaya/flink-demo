package cn.sliew.flink.dw.cep.dynamic;

import org.apache.flink.cep.dynamic.processor.PatternProcessor;
import org.apache.flink.cep.dynamic.processor.PatternProcessorDiscoverer;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.annotation.Nullable;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * The JDBC implementation of the {@link PeriodicPatternProcessorDiscovererFactory} that creates the
 * {@link CustomJDBCPeriodicPatternProcessorDiscoverer} instance.
 *
 * @param <T> Base type of the elements appearing in the pattern.
 */
public class CustomJDBCPeriodicPatternProcessorDiscovererFactory<T>
        extends PeriodicPatternProcessorDiscovererFactory<T> {

    private final SqlSessionFactory sessionFactory;

    public CustomJDBCPeriodicPatternProcessorDiscovererFactory(
            final SqlSessionFactory sessionFactory,
            @Nullable final List<PatternProcessor<T>> initialPatternProcessors,
            @Nullable final Long intervalMillis) {
        super(initialPatternProcessors, intervalMillis);
        this.sessionFactory = requireNonNull(sessionFactory);
    }

    @Override
    public PatternProcessorDiscoverer<T> createPatternProcessorDiscoverer(
            ClassLoader userCodeClassLoader) throws Exception {
        return new CustomJDBCPeriodicPatternProcessorDiscoverer<>(
                sessionFactory,
                userCodeClassLoader,
                this.getInitialPatternProcessors(),
                getIntervalMillis());
    }
}
