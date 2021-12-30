package cn.sliew.demo.canal.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import top.javatool.canal.client.annotation.CanalTable;
import top.javatool.canal.client.handler.EntryHandler;

import java.util.Map;

@Slf4j
@CanalTable(value = "t_user")
@Component
public class DefaultEntryHandler implements EntryHandler<Map<String, String>> {

    @Override
    public void insert(Map<String, String> map) {
        log.info("增加 {}", map);
    }

    @Override
    public void update(Map<String, String> before, Map<String, String> after) {
        log.info("修改 before {}", before);
        log.info("修改 after {}", after);
    }

    @Override
    public void delete(Map<String, String> map) {
        log.info("删除 {}", map);
    }
}
