package cn.sliew.flink.dw.support.util;

import cn.sliew.flink.dw.support.config.JdbcConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public enum MybatisUtil {
    ;

    private static SqlSessionFactory SQL_SESSION_FACTORY = null;

    public static SqlSessionFactory getSqlSessionFactory(ParameterTool tool) {
        if (SQL_SESSION_FACTORY != null) {
            return SQL_SESSION_FACTORY;
        }
        synchronized (MybatisUtil.class) {
            if (SQL_SESSION_FACTORY != null) {
                return SQL_SESSION_FACTORY;
            }
            SQL_SESSION_FACTORY = createSqlSessionFactory(tool, createDataSource(tool, "default"), getMapperXmls(tool));
        }
        return SQL_SESSION_FACTORY;
    }

    private static HikariDataSource createDataSource(ParameterTool tool, String name) {
        return createDataSource(ParameterToolUtil.getJdbcConfig(tool, name), name);
    }

    private static HikariDataSource createDataSource(JdbcConfig jdbcConfig, String name) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setPoolName(name);
        dataSource.setDriverClassName(jdbcConfig.getDriver());
        dataSource.setJdbcUrl(jdbcConfig.getUrl());
        dataSource.setUsername(jdbcConfig.getUser());
        dataSource.setPassword(jdbcConfig.getPassword());
        dataSource.setMaximumPoolSize(20);
        dataSource.setConnectionTimeout(100000);
        dataSource.setMinimumIdle(1);
        dataSource.setIdleTimeout(60000);
        dataSource.setConnectionInitSql("select 1");
        return dataSource;
    }

    private static List<String> getMapperXmls(ParameterTool tool) {
        try {
            String mapperLocation = tool.get("mybatis.mapper-location");
            if (!mapperLocation.endsWith("/")) {
                mapperLocation = mapperLocation + "/";
            }
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            URL mapperResources = classLoader.getResource(mapperLocation);
            List<String> mapperXmls = new ArrayList<>();
            if (mapperResources != null) {
                Path dataPath = Paths.get(mapperResources.toURI());
                String mapperPath = mapperLocation;
                Files.walk(dataPath)
                        .filter(Files::isRegularFile)
                        .forEach(filePath -> {
                            String fileName = filePath.getFileName().toString();
                            mapperXmls.add(mapperPath + fileName);
                        });
            }
            return mapperXmls;
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private static SqlSessionFactory createSqlSessionFactory(ParameterTool tool, HikariDataSource dataSource, List<String> mapperXmls) {
        try {
            Configuration configuration = new Configuration();
            Environment environment = new Environment(dataSource.getPoolName(), new JdbcTransactionFactory(), dataSource);
            configuration.setEnvironment(environment);
            configuration.setCacheEnabled(false);
//            configuration.setVariables();
            addMappers(configuration, mapperXmls);
            return new DefaultSqlSessionFactory(configuration);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addMappers(Configuration configuration, List<String> xmls) throws IOException {
        for (String xml : xmls) {
            InputStream resource = Resources.getResourceAsStream(xml);
            XMLMapperBuilder builder = new XMLMapperBuilder(resource, configuration, xml, configuration.getSqlFragments());
            builder.parse();
        }
    }
}
