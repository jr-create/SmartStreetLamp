package xiaohongshu;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Scanner;
import java.util.Stack;

/**
 * @author 29375-wjr
 * @Package: PACKAGE_NAME
 * @ClassName: Main
 * @create 2022-03-27 20:47
 * @Description:
 */

public class Main {
    public static void main(String[] args) {
        // xx();
        sqrt();
        // System.out.println((2.82842712474619 + 2.8284271247461903)+" "+(2.82842712474619 + 2.8284271247461903)/ 2);

        // trap();
    }

    /**
     * 接雨水
     */
    private static void trap() {
        Scanner sc = new Scanner(System.in);
        String nextLine = sc.nextLine();
        String substring = nextLine.substring(1, nextLine.length() - 1);
        String[] split = substring.split(",");
        int[] nums = new int[split.length];
        for (int i = 0; i < split.length; i++) {
            nums[i] = Integer.parseInt(split[i]);
        }
        System.out.println(Arrays.toString(nums));

        Stack<Integer> stack = new Stack<>();
        int result = 0;
        for (int i = 0; i < nums.length; i++) {
            while (!stack.isEmpty() && nums[i] > nums[stack.peek()]) { //当前高度需要大于前一个高度，才可能有水坑
                System.out.println("处理前：" + stack + "当前：" + i);
                Integer preIndex = stack.pop();//取出前一个位置，将水坑的位置取出缓存
                if (stack.isEmpty()) break;//判断是否为空，如果为空，表示没有水，不为空，表示有水
                Integer prePreIndexx = stack.peek();//水坑前的高度
                //当前高的位置 和 水坑前的高度比，取最小值，然后减去水坑的高度，就是水的容量
                int min = Math.min(nums[i], nums[prePreIndexx]) - nums[preIndex];
                int wid = i - 1 - prePreIndexx;//获取水的宽度=当前位置减去水坑前的位置-1（要减去当前位置，所以-1）
                result += wid * min; //添加水滴
                System.out.println("处理后：" + stack + "当前：" + i);
            }
            stack.push(i);
        }
        System.out.println(result);
    }

    /**
     * 求平方根
     */
    private static void xx() {
        Scanner sc = new Scanner(System.in);
        Double num = sc.nextDouble();
        double left = 0;
        double right = num;
        double pre = 0;
        // while((right - left) > 0.000000001){
        while (left <= right) {
            double mid = (right + left) / 2.0;
            if (pre == mid) break;
            if (num / mid < mid) {
                right = mid;
            } else if (num / mid > mid) {
                left = mid;
            }
            pre = mid;
        }
        System.out.println("二分法：" + pre + " " + pre * pre);
        double sqrt = Math.sqrt(num);
        System.out.println();
        System.out.println("对比Math.sqrt：" + sqrt + " " + sqrt * sqrt);
    }

    /**
     * BigDecimal求平方根
     */
    public static void sqrt() {
        Scanner sc = new Scanner(System.in);
        Double num = sc.nextDouble();
        BigDecimal right = new BigDecimal(num);
        BigDecimal left = new BigDecimal(0);
        while (left.compareTo(right) < 1) {
            BigDecimal mid = (left.add(right)).divide(new BigDecimal(2));
            BigDecimal multiply = mid.multiply(mid);
            int compare = multiply.compareTo(new BigDecimal(num));
            if (compare == 1) {
                right = mid;
            } else if (compare == -1) {
                left = mid;
            }
            System.out.println(mid+" "+ multiply);
        }
    }

    /**
     * 牛顿迭代法
     * @param t
     * @param precise
     * @return
     */
    public static double sqrt_(double t, Double precise) {
        double x0 = t, x1, differ,
                prec = precise != null ? precise : 1e-7;

        while (true) {
            x1 = (x0 * x0 + t) / (2 * x0);
            differ = x1 * x1 - t;

            if (differ <= prec && differ >= -prec) {
                return x1;
            }
            x0 = x1;
        }
    }
}
