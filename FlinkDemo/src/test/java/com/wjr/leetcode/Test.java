package com.wjr.leetcode;

import java.util.*;

public class Test {
    public static void main(String[] args) {
        ArrayList<List<Integer>> lists = new ArrayList<>();
        ArrayList<Integer> list = new ArrayList<>();
        list.add(1);
        ArrayList<Integer> list1 = new ArrayList<>();
        list1.add(1);
        lists.add(list);
        System.out.println(lists.contains(list1));
//        lists.add(list1);
        Set<List<Integer>> res = new HashSet<>();
        lists.addAll(res);
        System.out.println();
        int nump[] = new int[10];
        nump[0] = 1;
        nump[1] = 2;
        nump[2] = 3;
        nump[9] = 3;

        int sum = Arrays.stream(nump).sum();
        OptionalInt min = Arrays.stream(nump, 1, 10).min();
        System.out.println(min.getAsInt());

    }
}
