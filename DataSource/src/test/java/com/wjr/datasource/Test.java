package com.wjr.datasource;

import java.util.*;

/**
 * @author Lenovo-wjr
 * @Package: com.wjr.datasource
 * @ClassName: Test
 * @create 2022-02-27 17:52
 * @Description:
 */
class Test {
    public static void main(String[] args) {
        System.out.println(permute(new int[]{1, 3, 4}));
    }
    public static List<List<Integer>> permute(int[] nums) {
        List<List<Integer>> list = new ArrayList<>();
         permSort(nums,0,nums.length,list);
        return list;
    }

    public static void permSort(int[] nums, int start, int end, List<List<Integer>> list){
        if(start==end){
            ArrayList<Integer> arrList = new ArrayList<>();
            for (int num : nums) {
                arrList.add(num);
            }
            list.add(arrList);
            return ;
        }
        for(int i = start;i<end;i++){
            swap(nums,start,i);
            permSort(nums,start+1,end,list);
            swap(nums,start,i);
        }
    }
    public static void swap(int[] nums, int i, int j){
        int temp = nums[i] ;
        nums[i] = nums[j];
        nums[j] = temp;
    }
}
