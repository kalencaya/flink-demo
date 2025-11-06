package cn.sliew.flink.dw.job.inspection.rule.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombineDTO {

    private String direction;
    private String type;
    private String quantityType;
    private int quantity;
    private String unit;
}
