package cn.sliew.flink.dw.support.util;

import cn.sliew.flink.dw.common.JacksonUtil;
import cn.sliew.flink.dw.support.config.JdbcConfig;
import cn.sliew.flink.dw.support.config.KafkaTopicConfig;
import cn.sliew.flink.dw.support.config.RedisConfig;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;

import java.io.IOException;
import java.util.Properties;

public enum ParameterToolUtil {
    ;

    public static final String DEFAULT_CONFIGFILE_NAME = "flink.properties";
    public static final String ENVIRONMENT_FILENAME_TEMPLATE = "flink-%s.properties";
    public static final String ENV_ACTIVE = "env.active";
    public static ParameterTool parameterTool = null;

    public static ParameterTool createParameterTool(String[] args) throws IOException {
        ParameterTool systemProperties = ParameterTool.fromSystemProperties();
        ParameterTool fromArgs = ParameterTool.fromArgs(args);
        ParameterTool defaultPropertiesFile = ParameterTool.fromPropertiesFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(DEFAULT_CONFIGFILE_NAME));
        String envActiveValue = getEnvActiveValue(systemProperties, fromArgs, defaultPropertiesFile);
        String currentEnvFileName = String.format(ENVIRONMENT_FILENAME_TEMPLATE, envActiveValue);
        ParameterTool currentEnvPropertiesFile = ParameterTool.fromPropertiesFile(Thread.currentThread().getContextClassLoader().getResourceAsStream(currentEnvFileName));
        parameterTool = currentEnvPropertiesFile.mergeWith(defaultPropertiesFile).mergeWith(fromArgs).mergeWith(systemProperties);
        System.out.println("全局配置: \n" + JacksonUtil.toJsonString(parameterTool.toMap()));
        return parameterTool;
    }

    /**
     * 按照优先级获取有效环境值
     */
    public static String getEnvActiveValue(ParameterTool systemProperties, ParameterTool fromArgs, ParameterTool defaultPropertiesFile) {
        String env;
        if (systemProperties.has(ENV_ACTIVE)) {
            env = systemProperties.get(ENV_ACTIVE);
        } else if (fromArgs.has(ENV_ACTIVE)) {
            env = fromArgs.get(ENV_ACTIVE);
        } else if (defaultPropertiesFile.has(ENV_ACTIVE)) {
            env = defaultPropertiesFile.get(ENV_ACTIVE);
        } else {
            throw new IllegalArgumentException(String.format("%s does not exist！ Please set up the environment. for example： flink.properties Add configuration env.active = dev", ENV_ACTIVE));
        }
        return env;
    }

    public static Properties getKafkaConsumerConfig(KafkaTopicConfig config) {
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getServers());
        String finalGid = String.format("%s_%s", config.getGid(), parameterTool.get("env.active"));
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, finalGid);
        properties.setProperty(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "5000");
        properties.setProperty(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, "60000");
        properties.setProperty(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, Integer.valueOf(1024 * 1024 * 10).toString());
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }

    private static Properties getKafkaProducerConfig(KafkaTopicConfig config) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, config.getServers());
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 30 * 1000);
        properties.put(ProducerConfig.RETRIES_CONFIG, 5);
        properties.put(ProducerConfig.RECONNECT_BACKOFF_MS_CONFIG, 3000);
        properties.setProperty(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "5242880");
        return properties;
    }

    public static KafkaTopicConfig getKafkaTopicConfig(ParameterTool tool, String topicPrefix, String serverPrefix) {

        return new KafkaTopicConfig(
                tool.get(String.format("%s.kafka.servers", serverPrefix)),
                tool.get(String.format("%s.%s.kafka.topic", topicPrefix, serverPrefix), null),
                tool.get(String.format("%s.%s.kafka.topic.gid", topicPrefix, serverPrefix), null),
                tool.get(String.format("%s.%s.topic.scan.startup.mode", topicPrefix, serverPrefix), null)
        );
    }

    public static RedisConfig getRedisConfig(ParameterTool tool, String prefix) {
        return new RedisConfig(
                tool.get(String.format("%s.redis.host", prefix)),
                tool.getInt(String.format("%s.redis.port", prefix)),
                tool.get(String.format("%s.redis.password", prefix)),
                tool.getInt(String.format("%s.redis.database", prefix))
        );
    }

    public static JdbcConfig getJdbcConfig(ParameterTool parameterTool, String prefix) {
        return new JdbcConfig(
                parameterTool.get(String.format("%s.jdbc.url", prefix)),
                parameterTool.get(String.format("%s.jdbc.user", prefix)),
                parameterTool.get(String.format("%s.jdbc.password", prefix)),
                parameterTool.get(String.format("%s.jdbc.driver", prefix)),
                parameterTool.getInt(String.format("%s.jdbc.flush.max.rows", prefix), 100),
                parameterTool.getInt(String.format("%s.jdbc.flush.interval", prefix), 500)
        );
    }
}
