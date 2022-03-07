package shopee;

/**
 * @author Lenovo-wjr
 * @Package: shopee
 * @ClassName: Main
 * @create 2022-03-07 19:41
 * @Description:
 */
public class Main {
    public static void main(String[] args) {
        // System.out.println(calDPDScore("NNNYYYNNYYYYYYYY"));
        System.out.println(GetMinCalculateCount(10,100,25,204));
        long[] ints = {2, 3, -2, 4, -1};
        System.out.println(GetSubArrayMaxProduct(ints));
    }


    public static int calDPDScore(String dpdInfo) {
        int result = 0;
        int temp = 0;
        for (int i = 0; i < dpdInfo.length(); i++) {
            char c = dpdInfo.charAt(i);
            if (c == 'Y') {
                temp++;
            } else {
                result = Math.max(result, temp);
                temp = 0;
            }
        }
        result = Math.max(result, temp);
        if (result == 0) {
            return 0;
        } else if (result <= 3) {
            return -10;
        } else if (result <= 7) {
            return -15;
        } else {
            return -25;
        }

    }

    /**
     * Note: 类名、方法名、参数名已经指定，请勿修改
     * <p>
     * <p>
     * 将 sourceX, sourceY 转换成 targetX, targetY 最少需要多少次计算
     *
     * @param sourceX long长整型  x初始值
     * @param sourceY long长整型  y初始值
     * @param targetX long长整型  x目标值
     * @param targetY long长整型  y目标值
     * @return long长整型
     */
    public static long GetMinCalculateCount(long sourceX, long sourceY, long targetX, long targetY) {
        long result = 0;
        long count = 0;
        while (targetX > 0) {
            if (targetX >= sourceX * 2) {
                targetX = targetX >> 1;
                targetY = targetY >> 1;
            }else{
                targetX = targetX % sourceX;
                targetY = targetY % sourceY;
            }
            System.out.println(targetX + " " + targetY);
            if (targetX == targetY && targetX <= sourceX) {
                count = targetX + count ;
                result += count;
                return result;
            } else {
                count++;
            }
        }
        return -1;
    }

    /**
     * Note: 类名、方法名、参数名已经指定，请勿修改
     * <p>
     * <p>
     * 找到数组中乘积最大的连续子数组，并返回乘积
     *
     * @param nums long长整型 一维数组 原始数组
     * @return long长整型
     */
    public static long GetSubArrayMaxProduct(long[] nums) {
        long max = nums[0];
        long min = nums[0];
        long result = nums[0];
        for (int i = 1; i < nums.length; i++) {
            long tmax = max, tmin = min;
            max = Math.max(tmax * nums[i], Math.max(nums[i], tmin * nums[i]));
            min = Math.min(tmin * nums[i], Math.min(nums[i], tmax * nums[i]));
            result = Math.max(result, max);
        }
        return result;
    }

}
