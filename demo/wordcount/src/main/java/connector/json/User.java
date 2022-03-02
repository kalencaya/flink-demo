package connector.json;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Date dateType = new Date();

    private Time timeType = new Time(System.currentTimeMillis());

    private Timestamp timestampType = new Timestamp(System.currentTimeMillis());
}
