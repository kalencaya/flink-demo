package cn.sliew.flink.dw.job.inspection.source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    // 用户id
    private int id;
    // 用户名
    private String name;
    // 0: 浏览，1: 购买
    private int action;
    // 商品id
    private int productionId;
    // 事件事件，毫秒级时间戳
    private long eventTime;

    public static Event fromString(String eventStr) {
        String[] split = eventStr.split(",");
        return new Event(
                Integer.parseInt(split[0]),
                split[1],
                Integer.parseInt(split[2]),
                Integer.parseInt(split[3]),
                Long.parseLong(split[4]));
    }
}
