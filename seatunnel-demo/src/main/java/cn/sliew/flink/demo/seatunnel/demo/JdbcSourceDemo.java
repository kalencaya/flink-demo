package cn.sliew.flink.demo.seatunnel.demo;

import org.apache.seatunnel.Seatunnel;
import org.apache.seatunnel.config.command.CommandLineArgs;

import java.net.URL;

import static org.apache.seatunnel.utils.Engine.FLINK;

public class JdbcSourceDemo {

    public static void main(String[] args) throws Exception {
        URL conf = JdbcSourceDemo.class.getClassLoader().getResource("flink.batch.conf");
        CommandLineArgs flinkArgs = new CommandLineArgs(conf.getPath(), false);
        Seatunnel.run(flinkArgs, FLINK);
    }
}
