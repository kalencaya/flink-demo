package cn.sliew.flink.dw.job.inspection.dynamic;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PatternVO {

    private Long id;
    private String pattern;
    private String function;
    private Integer version;
}
