package cn.sliew.flink.dw.support.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.KafkaSourceBuilder;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.streaming.connectors.kafka.config.StartupMode;
import org.apache.kafka.clients.consumer.OffsetResetStrategy;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public enum KafkaUtil {
    ;

    public static final Charset UTF_8 = StandardCharsets.UTF_8;

    public static <T> KafkaSourceBuilder<T> getSource(Properties kafkaProperties) {
        return (KafkaSourceBuilder<T>) KafkaSource.builder().setProperties(kafkaProperties);
    }

    public static OffsetsInitializer getOffset(String startFrom) {
        if (StartupMode.LATEST.name().equalsIgnoreCase(startFrom)) {
            log.info("start mode: {}", StartupMode.LATEST.name());
            return OffsetsInitializer.latest();
        } else if (StartupMode.EARLIEST.name().equalsIgnoreCase(startFrom)) {
            log.info("start mode: {}", StartupMode.EARLIEST.name());
            return OffsetsInitializer.earliest();
        } else if (StringUtils.isNumeric(startFrom)) {
            log.info("start from timestamp: {}", startFrom);
            return OffsetsInitializer.timestamp(Long.parseLong(startFrom));
        } else if (StartupMode.GROUP_OFFSETS.name().equalsIgnoreCase(startFrom)) {
            log.info("start from: {}", StartupMode.GROUP_OFFSETS.name());
            return OffsetsInitializer.committedOffsets();
        }

        log.info("start mode, use default: {}, backup by: {}", StartupMode.GROUP_OFFSETS.name(), StartupMode.EARLIEST.name());
        return OffsetsInitializer.committedOffsets(OffsetResetStrategy.EARLIEST);
    }

    public static KafkaSource<String> getKafkaSource(String groupId, String topicName, ParameterTool parameterTool) {
        String finalGroupId = formatGroupId(groupId, parameterTool);
        Properties kafkaProperties = ParameterToolUtil.getKafkaConsumerConfig(finalGroupId);
        OffsetsInitializer offsetsInitializer = KafkaUtil.getOffset(parameterTool.get("scan.startup.mode"));
        return KafkaUtil.<String>getSource(kafkaProperties)
                .setTopics(topicName)
                .setValueOnlyDeserializer(new SimpleStringSchema())
                .setStartingOffsets(offsetsInitializer)
                .build();
    }

    public static String formatGroupId(String groupId, ParameterTool parameterTool) {
//        return String.format("%s_%s", groupId, parameterTool.get("env.active").toUpperCase());
        return groupId;
    }
}
