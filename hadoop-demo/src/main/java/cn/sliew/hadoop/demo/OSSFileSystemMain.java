package cn.sliew.hadoop.demo;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class OSSFileSystemMain {

    public static void main(String[] args) {
        Configuration conf = new Configuration();
        conf.set("fs.defaultFS", "hdfs://hadoop:9000");
        FileSystem fs = FileSystem.newInstance(conf);
        final Path workingDirectory = fs.getWorkingDirectory();
        final Path homeDirectory = fs.getHomeDirectory();
        System.out.println(workingDirectory);
        System.out.println(homeDirectory);
        System.out.println(fs.getScheme());
    }
}
