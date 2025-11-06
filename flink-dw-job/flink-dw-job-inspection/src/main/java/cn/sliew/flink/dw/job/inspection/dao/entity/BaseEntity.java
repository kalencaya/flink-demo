package cn.sliew.flink.dw.job.inspection.dao.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BaseEntity {

    /**
     * 主键
     */
    private Long id;
    /**
     * 创建者
     */
    private String createUserId;
    /**
     * 更新者
     */
    private String updateUserId;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    /**
     * 逻辑删除，0:正常，1:删除
     */
    private Boolean deleted;
}
