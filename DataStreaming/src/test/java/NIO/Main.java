package NIO;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author 29375-wjr
 * @Package: NIO
 * @ClassName: Main
 * @create 2022-04-15 19:01
 * @Description:
 */
public class Main {
    public static void main(String[] args) {
        shortestPath();
        // Scanner sc = new Scanner(System.in);
        // String string = sc.nextLine();
        // String chars = sc.nextLine();
        // System.out.println(shortestSubstring(string, chars));
    }

    private static void shortestPath() {
        Scanner sc = new Scanner(System.in);
        String line = sc.next();
        String[] split = line.split(",");
        if (split.length <= 1) {
            System.out.println(0);
            return;
        }
        ArrayList<Integer> nums = new ArrayList<>();
        ArrayList<ArrayList<Integer>> arrayLists = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            int num = Integer.parseInt(split[i]);
            if (nums.contains(num)) {
                int index = nums.indexOf(num);

                ArrayList<Integer> arrayList = arrayLists.get(index);
                arrayList.add(i);
            } else {
                ArrayList<Integer> e = new ArrayList<>();
                e.add(i);
                arrayLists.add(e);
            }
            nums.add(num);
            System.out.println(arrayLists);

        }
        ArrayList<Integer> tailIndex = arrayLists.get(nums.size() - 1);
        int result = Integer.MAX_VALUE;
        int r = 0;
        for (ArrayList<Integer> values : arrayLists) {
            int min = Integer.MAX_VALUE;
            for (int i = 0; i < values.size(); i++) {
                for (Integer index : tailIndex) {
                    System.out.println("中间结果:" + index + " " + values.get(i) + " " + i);
                    min = Math.min(Math.abs(index - values.get(i)) + i, min);
                }
            }
            System.out.println("测试；" + min + " " + result);
            result = Math.min(result, min + r++);
        }
        System.out.println(result);
    }

    public static String shortestSubstring(String str, String chars) {
        StringBuilder sb = new StringBuilder();
        String result = str;
        int index = str.indexOf(chars.charAt(0));
        while (index != -1) {
            System.out.println("位置："+index);
            char[] strChars = str.toCharArray();
            for (int j = index; j < strChars.length; j++) {
                sb.append(strChars[j]);
                if (j == str.indexOf(chars.charAt(chars.length() - 1))) break;
            }
            index = str.indexOf(chars.charAt(0), index+1);
            System.out.println("string："+sb.toString());
            result = (result.length() < sb.length()) ? result : sb.toString();
            sb.delete(0,sb.length());
        }

        return result;
    }
}
