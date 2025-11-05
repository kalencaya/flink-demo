package cn.sliew.flink.dw.cep.dao.entity;

import lombok.Data;

@Data
public class InspectionRuleEntity extends BaseEntity {

    private String function;
    private String pattern;
    private Integer version;
}
