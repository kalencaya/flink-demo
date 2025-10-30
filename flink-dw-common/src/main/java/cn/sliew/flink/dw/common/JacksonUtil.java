package cn.sliew.flink.dw.common;

import org.apache.commons.lang3.StringUtils;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.core.type.TypeReference;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.SerializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.type.CollectionType;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum JacksonUtil {
    ;

    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonUtil.class);

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule())
                .registerModule((new Jdk8Module()).configureAbsentsAsNulls(true))
                .disable(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static String toJsonString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOGGER.error("json 序列化失败 object: {}", object, e);
            throw new RuntimeException("json 序列化失败 object: " + object, e);
        }
    }

    public static <T> T parseJsonString(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error("json 反序列化失败 clazz: {}, json: {}", clazz.getName(), json, e);
            throw new RuntimeException("json 反序列化失败 clazz: " + clazz.getName() + ", json: " + json, e);
        }
    }

    public static <T> T parseJsonString(String json, TypeReference<T> reference) {
        try {
            return OBJECT_MAPPER.readValue(json, reference);
        } catch (JsonProcessingException e) {
            LOGGER.error("json 反序列化失败 clazz: {}, json: {}", reference.getType().getTypeName(), json, e);
            throw new RuntimeException("json 反序列化失败 clazz: " + reference.getType().getTypeName() + ", json: " + json, e);
        }
    }

    public static <T> List<T> parseJsonArray(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json)) {
            return Collections.emptyList();
        }

        try {
            CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
            return OBJECT_MAPPER.readValue(json, listType);
        } catch (Exception e) {
            LOGGER.error("json 反序列化失败 clazz: {}, json: {}", clazz.getName(), json, e);
        }

        return Collections.emptyList();
    }

    public static <T> T convertValue(JsonNode json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.convertValue(json, clazz);
        } catch (Exception e) {
            LOGGER.error("json 反序列化失败 clazz: {}, json: {}", clazz.getName(), json.toString(), e);
            throw new RuntimeException("json 反序列化失败 clazz: " + clazz.getName() + ", json: " + json.toString(), e);
        }
    }

    public static ArrayNode createArrayNode() {
        return OBJECT_MAPPER.createArrayNode();
    }

    public static ObjectNode createObjectNode() {
        return OBJECT_MAPPER.createObjectNode();
    }

    public static JsonNode toJsonNode(Object obj) {
        return OBJECT_MAPPER.valueToTree(obj);
    }

    public static JsonNode toJsonNode(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException e) {
            LOGGER.error("json 反序列化失败 json: {}", json, e);
            throw new RuntimeException("json 反序列化失败 json: " + json, e);
        }
    }

    /**
     * 注意在 bean 上面添加 {@link JsonIgnoreProperties}
     */
    public static <S, T> T deepCopy(S source, Class<T> clazz) {
        try {
            JsonNode jsonNode = OBJECT_MAPPER.valueToTree(source);
            return OBJECT_MAPPER.treeToValue(jsonNode, clazz);
        } catch (JsonProcessingException e) {
            LOGGER.error("属性深拷贝异常 source: {}, target: {}", source, clazz.getName(), e);
            throw new RuntimeException("属性深拷贝异常 source: " + source + ", target: " + clazz.getName(), e);
        }
    }

    public static boolean checkJsonValid(String json) {
        if (StringUtils.isEmpty(json)) {
            return false;
        }

        try {
            OBJECT_MAPPER.readTree(json);
            return true;
        } catch (IOException e) {
            LOGGER.error("check json object valid exception!", e);
        }

        return false;
    }

}
