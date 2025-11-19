package cn.sliew.flink.dw.job.inspection.condition;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class DuplicateElementChecker<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 检测数组中是否存在连续windowSize个元素内有minDuplicate个重复元素
     *
     * @param arr          输入数组
     * @param windowSize   窗口大小
     * @param minDuplicate 最小重复次数
     * @return 如果存在满足条件的窗口返回true，否则返回false
     */
    public boolean hasDuplicateInWindow(T[] arr, int windowSize, int minDuplicate) {
        int n = arr.length;
        if (n < windowSize || minDuplicate > windowSize) {
            return false;
        }

        Map<T, Integer> freq = new HashMap<>();

        // 初始化第一个窗口
        for (int i = 0; i < windowSize; i++) {
            freq.put(arr[i], freq.getOrDefault(arr[i], 0) + 1);
            if (freq.get(arr[i]) >= minDuplicate) {
                return true;
            }
        }

        // 滑动窗口
        for (int i = 1; i <= n - windowSize; i++) {
            // 移除离开窗口的元素
            T leftElement = arr[i - 1];
            freq.put(leftElement, freq.get(leftElement) - 1);
            if (freq.get(leftElement) == 0) {
                freq.remove(leftElement);
            }

            // 添加进入窗口的元素
            T rightElement = arr[i + windowSize - 1];
            freq.put(rightElement, freq.getOrDefault(rightElement, 0) + 1);

            // 检查当前窗口
            if (freq.get(rightElement) >= minDuplicate) {
                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {
        DuplicateElementChecker solution = new DuplicateElementChecker();

        // 测试用例
        Object[][] testCases = {
                // {输入数组, 窗口大小, 最小重复数, 期望结果}
                {new String[]{"a", "b", "c", "d", "e"}, 5, 3, false},
                {new String[]{"a", "a", "a", "b", "c"}, 5, 3, true},
                {new String[]{"a", "b", "a", "c", "a"}, 5, 3, true},
                {new String[]{"a", "a", "b", "b", "c"}, 5, 3, false},
                {new String[]{"a", "a", "a", "a", "b"}, 5, 4, true},
                {new String[]{"a", "b", "a", "b", "a"}, 5, 3, true},
                {new String[]{"a", "a", "b", "b", "c", "c"}, 4, 2, true},
                {new String[]{"a", "b", "c", "d"}, 5, 3, false}, // 数组长度不足
                {new String[]{"a", "a", "a", "b", "b", "c"}, 3, 3, true},
        };

        System.out.println("=== 测试通用方法 ===");
        for (int i = 0; i < testCases.length; i++) {
            String[] input = (String[]) testCases[i][0];
            int windowSize = (int) testCases[i][1];
            int minDuplicate = (int) testCases[i][2];
            boolean expected = (boolean) testCases[i][3];

            boolean result = solution.hasDuplicateInWindow(input, windowSize, minDuplicate);
            String status = result == expected ? "✓" : "✗";

            System.out.printf("Test %d: %s Window=%d, MinDup=%d, Input: %s, Expected: %s, Got: %s%n",
                    i + 1, status, windowSize, minDuplicate,
                    Arrays.toString(input), expected, result);
        }
    }
}