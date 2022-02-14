package connector.csv;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @JsonProperty(value = "订单id", index = 1)
    private Long orderId;

    @JsonProperty(value = "价格", index = 2)
    private Long price;

    @JsonProperty(value = "支付金额", index = 3)
    private Double payAmount;

    @JsonProperty(value = "买家名称", index = 4)
    private String buyerName;

    @JsonProperty(value = "下单时间", index = 5)
    private Date createTime;
}
