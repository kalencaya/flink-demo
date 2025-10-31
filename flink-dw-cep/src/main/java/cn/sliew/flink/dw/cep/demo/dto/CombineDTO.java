package cn.sliew.flink.dw.cep.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CombineDTO {
    private String direction;
    private String type;
    private int quantity;
    private String unit;
}
