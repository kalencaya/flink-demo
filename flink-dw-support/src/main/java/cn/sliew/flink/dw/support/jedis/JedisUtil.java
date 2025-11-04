package cn.sliew.flink.dw.support.jedis;

public enum JedisUtil {
    ;

    public static final Long ONE_SECOND = 1L;
    public static final Long ONE_MINUTE = ONE_SECOND * 60;
    public static final Long ONE_HOUR = ONE_MINUTE * 60;
    public static final Long ONE_DAY = ONE_HOUR * 24;
    public static final Long ONE_WEEK = ONE_DAY * 3;
    public static final Long ONE_MONTH = ONE_DAY * 30;
    public static final Long ONE_YEAR = ONE_MONTH * 12;

    public static final String PREFIX = "flink:";
    public static final String CEP_PREFIX = PREFIX + "cep:";

    public static final String DUPLICATE_MESSAGE_KEY = CEP_PREFIX + "duplicate_message:";
    public static final Long DUPLICATE_MESSAGE_EXPIRATION = ONE_DAY;

}
