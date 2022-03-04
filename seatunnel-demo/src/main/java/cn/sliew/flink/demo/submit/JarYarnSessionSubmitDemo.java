package cn.sliew.flink.demo.submit;

import java.net.InetAddress;

public class JarYarnSessionSubmitDemo {

    public static void main(String[] args) throws Exception {
        InetAddress addr = InetAddress.getLocalHost();
        System.out.println(addr.getHostAddress());
    }
}
