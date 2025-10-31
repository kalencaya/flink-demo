package cn.sliew.flink.dw.cep.demo;

import cn.sliew.flink.dw.cep.condition.DumyCondition;
import cn.sliew.flink.dw.cep.demo.dto.CombineDTO;
import org.apache.flink.cep.nfa.aftermatch.AfterMatchSkipStrategy;
import org.apache.flink.cep.pattern.Pattern;
import org.apache.flink.streaming.api.windowing.time.Time;

import java.time.Duration;

public enum PatternUtil {
    ;

    public static <T> Pattern<T, T> joinCombine(CombineDTO combineDTO, Pattern<T, T> parent, Pattern<T, T> child) {
        Pattern<T, T> combinePattern = convertCombine(combineDTO);
        String direction = combineDTO.getDirection();
        switch (direction) {
            case "before":
//                return combinePattern.notFollowedBy(parent).next(child);
                return null;
            case "after":
                return parent.followedBy(combinePattern).next(child);
            default:
                throw new RuntimeException("unknown combine direction type: " + direction);
        }
    }

    private static <T> Pattern<T, T> joinBeforeCombine(CombineDTO combineDTO, Pattern<T, T> parent, Pattern<T, T> child) {
        String type = combineDTO.getType();
        switch (type) {
            case "句":
                return parent.notFollowedBy("n 句").where(DumyCondition.getInstance()).times(1, 4).next(child);
            case "秒":
                return convertWithinPattern(combineDTO);
            default:
                throw new RuntimeException("unknown combine type: " + type);
        }
    }

    private static <T> Pattern<T, T> convertCombine(CombineDTO combineDTO) {
        String type = combineDTO.getType();
        switch (type) {
            case "句":
                return convertCountPattern(combineDTO);
            case "秒":
                return convertWithinPattern(combineDTO);
            default:
                throw new RuntimeException("unknown combine type: " + type);
        }
    }

    private static <T> Pattern<T, T> convertCountPattern(CombineDTO combineDTO) {
        Pattern<T, T> dumyPattern = Pattern.<T>begin("n 句", AfterMatchSkipStrategy.noSkip())
                .where(DumyCondition.getInstance());
        if (combineDTO.getQuantity() > 1) {
            dumyPattern = dumyPattern.times(1, combineDTO.getQuantity() - 1).optional();
        }
        return dumyPattern;
    }

    private static <T> Pattern<T, T> convertWithinPattern(CombineDTO combineDTO) {
        Pattern<T, T> dumyPattern = Pattern.<T>begin("n 秒|分", AfterMatchSkipStrategy.noSkip())
                .where(DumyCondition.getInstance());
        if (combineDTO.getQuantity() > 0) {
            long seconds = 0L;
            switch (combineDTO.getUnit()) {
                case "sec":
                    seconds = combineDTO.getQuantity();
                    break;
                case "minute":
                    seconds = Duration.ofMinutes(combineDTO.getQuantity()).toSeconds();
                    break;
                default:
                    throw new RuntimeException("unknown combine unit: " + combineDTO.getUnit());
            }
            dumyPattern = dumyPattern.within(Time.seconds(seconds));
        }
        return dumyPattern;
    }

}
