package cn.sliew.flink.dw.job.inspection.rule;

import cn.sliew.flink.dw.job.inspection.rule.dto.CombineDTO;
import cn.sliew.flink.dw.job.inspection.rule.dto.PatternDTO;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.cep.pattern.conditions.BooleanConditions;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;
import java.util.Objects;

public enum PatternUtil {
    ;

    public static <T> Pattern<T, T> convertPattern(PatternDTO patternDTO) {
        Pattern<T, T> pattern = Pattern.<T>begin(patternDTO.getKey())
                .where(ConditionUtil.convert(patternDTO.getRule()));
        if (Objects.nonNull(patternDTO.getChild())) {
            Pattern<T, T> childPattern = convertPattern(patternDTO.getChild());
            if (Objects.nonNull(patternDTO.getCombine())) {
                return joinCombine(patternDTO.getCombine(), pattern, childPattern);
            } else {
                // todo 没有配置，默认 松散连续
                return pattern.followedBy(childPattern);
            }
        }
        return pattern;
    }

    public static <T> Pattern<T, T> joinCombine(CombineDTO combineDTO, Pattern<T, T> parent, Pattern<T, T> child) {
        Pattern<T, T> combinePattern = convertCombine(combineDTO);
        String direction = combineDTO.getDirection();
        switch (direction) {
            case "before":
                throw new UnsupportedOperationException("unsupport combine direction type: " + direction);
            case "after":
                return parent.followedBy(combinePattern).next(child);
            default:
                throw new RuntimeException("unknown combine direction type: " + direction);
        }
    }

    private static <T> Pattern<T, T> convertCombine(CombineDTO combineDTO) {
        String quantityType = combineDTO.getQuantityType();
        switch (quantityType) {
            case "句":
                return convertCountPattern(combineDTO);
            case "秒":
                return convertWithinPattern(combineDTO);
            default:
                throw new RuntimeException("unknown combine type: " + quantityType);
        }
    }

    private static <T> Pattern<T, T> convertCountPattern(CombineDTO combineDTO) {
        Pattern<T, T> dumyPattern = Pattern.<T>begin("n 句", AfterMatchSkipStrategy.noSkip())
                .where(BooleanConditions.trueFunction());
        if (combineDTO.getQuantity() > 1) {
            dumyPattern = dumyPattern.times(1, combineDTO.getQuantity() - 1).optional();
        }
        return dumyPattern;
    }

    private static <T> Pattern<T, T> convertWithinPattern(CombineDTO combineDTO) {
        Pattern<T, T> dumyPattern = Pattern.<T>begin("n 秒|分", AfterMatchSkipStrategy.noSkip())
                .where(BooleanConditions.trueFunction());
        if (combineDTO.getQuantity() > 0) {
            long seconds = 0L;
            switch (combineDTO.getUnit()) {
                case "sec":
                    seconds = combineDTO.getQuantity();
                    break;
                case "minute":
                    seconds = Duration.ofMinutes(combineDTO.getQuantity()).toMinutes() * 60;
                    break;
                default:
                    throw new RuntimeException("unknown combine unit: " + combineDTO.getUnit());
            }
            dumyPattern = dumyPattern.within(Time.seconds(seconds));
        }
        return dumyPattern;
    }

}
