package cn.sliew.flink.dw.job.inspection.dynamic;

import cn.sliew.flink.dw.cep.dynamic.DefaultPatternProcessor;
import cn.sliew.flink.dw.cep.dynamic.PeriodicPatternProcessorDiscoverer;
import cn.sliew.flink.dw.job.inspection.dao.entity.InspectionRuleEntity;
import cn.sliew.flink.dw.job.inspection.dao.mapper.InspectionRuleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.cep.dynamic.impl.json.deserializer.ConditionSpecStdDeserializer;
import org.apache.flink.cep.dynamic.impl.json.deserializer.NodeSpecStdDeserializer;
import org.apache.flink.cep.dynamic.impl.json.deserializer.TimeStdDeserializer;
import org.apache.flink.cep.dynamic.impl.json.spec.ConditionSpec;
import org.apache.flink.cep.dynamic.impl.json.spec.NodeSpec;
import org.apache.flink.cep.dynamic.processor.PatternProcessor;
import org.apache.flink.cep.functions.PatternProcessFunction;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.CollectionUtil;
import org.apache.flink.util.StringUtils;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * The JDBC implementation of the {@link PeriodicPatternProcessorDiscoverer} that periodically
 * discovers the rule updates from the database by using JDBC.
 *
 * @param <T> Base type of the elements appearing in the pattern.
 */
@Slf4j
public class CustomJDBCPeriodicPatternProcessorDiscoverer<T> extends PeriodicPatternProcessorDiscoverer<T> {

    private final SqlSessionFactory sqlSessionFactory;
    private final List<PatternProcessor<T>> initialPatternProcessors;
    private final ClassLoader userCodeClassLoader;
    private final ObjectMapper objectMapper;
    private Map<Long, PatternVO> latestPatternProcessors;

    /**
     * Creates a new using the given initial {@link PatternProcessor} and the time interval how
     * often to check the pattern processor updates.
     *
     * @param sqlSessionFactory        The mybatis sql session factory
     * @param initialPatternProcessors The list of the initial {@link PatternProcessor}.
     * @param intervalMillis           Time interval in milliseconds how often to check updates.
     */
    public CustomJDBCPeriodicPatternProcessorDiscoverer(
            final SqlSessionFactory sqlSessionFactory,
            final ClassLoader userCodeClassLoader,
            @Nullable final List<PatternProcessor<T>> initialPatternProcessors,
            @Nullable final Long intervalMillis) {
        super(intervalMillis);
        this.sqlSessionFactory = requireNonNull(sqlSessionFactory);
        this.initialPatternProcessors = initialPatternProcessors;
        this.userCodeClassLoader = userCodeClassLoader;
        objectMapper = new ObjectMapper().registerModule(new SimpleModule()
                        .addDeserializer(ConditionSpec.class, ConditionSpecStdDeserializer.INSTANCE)
                        .addDeserializer(Time.class, TimeStdDeserializer.INSTANCE)
                        .addDeserializer(NodeSpec.class, NodeSpecStdDeserializer.INSTANCE));
    }

    @Override
    public boolean arePatternProcessorsUpdated() {
        if (latestPatternProcessors == null
                && !CollectionUtil.isNullOrEmpty(initialPatternProcessors)) {
            return true;
        }
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            InspectionRuleMapper inspectionRuleMapper = session.getMapper(InspectionRuleMapper.class);
            List<InspectionRuleEntity> inspectionRuleEntities = inspectionRuleMapper.listAll();
            Map<Long, PatternVO> currentPatternProcessors = new HashMap<>();
            for (InspectionRuleEntity rule : inspectionRuleEntities) {
                if (currentPatternProcessors.containsKey(rule.getId())
                        && currentPatternProcessors.get(rule.getId()).getVersion() >= rule.getVersion()) {
                    continue;
                }

                currentPatternProcessors.put(
                        rule.getId(),
                        new PatternVO()
                                .setId(requireNonNull(rule.getId()))
                                .setVersion(rule.getVersion())
                                .setPattern(requireNonNull(rule.getPattern()))
                                .setFunction(rule.getFunction()));
            }

            if (latestPatternProcessors == null
                    || isPatternProcessorUpdated(currentPatternProcessors)) {
                latestPatternProcessors = currentPatternProcessors;
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            log.error("Pattern processor discoverer failed to check rule changes, will retry through next schedule", e);
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<PatternProcessor<T>> getLatestPatternProcessors() throws Exception {
        return latestPatternProcessors.values().stream()
                .map(
                        patternProcessor -> {
                            try {
                                String patternStr = patternProcessor.getPattern();
                                objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
                                PatternProcessFunction<T, ?> patternProcessFunction = null;
                                Long id = patternProcessor.getId();
                                int version = patternProcessor.getVersion();

                                if (!StringUtils.isNullOrWhitespaceOnly(patternProcessor.getFunction())) {
                                    patternProcessFunction =
                                            (PatternProcessFunction<T, ?>)
                                                    this.userCodeClassLoader
                                                            .loadClass(patternProcessor.getFunction())
                                                            .getConstructor(String.class, int.class)
                                                            .newInstance(id, version);
                                }
                                log.warn(
                                        objectMapper
                                                .writerWithDefaultPrettyPrinter()
                                                .writeValueAsString(patternProcessor.getPattern()));
                                return new DefaultPatternProcessor<>(
                                        patternProcessor.getId(),
                                        patternProcessor.getVersion(),
                                        patternStr,
                                        patternProcessFunction,
                                        this.userCodeClassLoader);
                            } catch (Exception e) {
                                log.error("Get the latest pattern processors of the discoverer failure. - ", e);
                            }
                            return null;
                        })
                .collect(Collectors.toList());
    }

    private boolean isPatternProcessorUpdated(
            Map<Long, PatternVO> currentPatternProcessors) {
        return latestPatternProcessors.size() != currentPatternProcessors.size()
                || !currentPatternProcessors.equals(latestPatternProcessors);
    }
}
