package exarrays;

import java.util.Arrays;
import java.util.PriorityQueue;

/**
 * Find the kth largest element in an unsorted array. Note that it is the kth largest element in the sorted order, not the kth distinct element.

 For example, given [3,2,1,5,6,4] and k = 2, return 5.
 */

public class KthSmallestElement {


    public static void main(String args[]){
        int[] nums = new int[]{3,2,1,5,6,4,9,26,0};
        int k = 2;
        System.out.println(findKthLargest(nums,2));
    }
    public static int findKthLargest(int[] nums, int k) {
        Arrays.sort(nums);
        return nums[k-1];
    }

    //This problem can also be solved by using quickselect, which is similar to quicksort.

    public int findKthLargest2(int[] nums, int k) {
        if (k < 1 || nums == null) {
            return 0;
        }

        return getKth(nums.length - k +1, nums, 0, nums.length - 1);
    }

    public int getKth(int k, int[] nums, int start, int end) {

        int pivot = nums[end];

        int left = start;
        int right = end;

        while (true) {

            while (nums[left] < pivot && left < right) {
                left++;
            }

            while (nums[right] >= pivot && right > left) {
                right--;
            }

            if (left == right) {
                break;
            }

            swap(nums, left, right);
        }

        swap(nums, left, end);

        if (k == left + 1) {
            return pivot;
        } else if (k < left + 1) {
            return getKth(k, nums, start, left - 1);
        } else {
            return getKth(k, nums, left + 1, end);
        }
    }

    public void swap(int[] nums, int n1, int n2) {
        int tmp = nums[n1];
        nums[n1] = nums[n2];
        nums[n2] = tmp;
    }

    /**
     * We can use a min heap to solve this problem. The heap stores the top k elements.
     * Whenever the size is greater than k, delete the min. Time complexity is O(nlog(k)).
     * Space complexity is O(k) for storing the top k numbers.
     */

    public int findKthLargest3(int[] nums, int k) {
        PriorityQueue<Integer> q = new PriorityQueue<Integer>(k);
        for(int i: nums){
            q.offer(i);

            if(q.size()>k){
                q.poll();
            }
        }

        return q.peek();
    }

}
