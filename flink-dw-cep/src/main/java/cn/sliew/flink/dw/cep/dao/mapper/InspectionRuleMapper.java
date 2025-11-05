package cn.sliew.flink.dw.cep.dao.mapper;

import cn.sliew.flink.dw.cep.dao.entity.InspectionRuleEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InspectionRuleMapper {

    List<InspectionRuleEntity> listAll();
}
