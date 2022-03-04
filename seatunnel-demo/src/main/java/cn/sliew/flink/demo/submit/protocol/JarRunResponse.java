package cn.sliew.flink.demo.submit.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JarRunResponse {

    @JsonProperty("jobid")
    private String jobID;
}
