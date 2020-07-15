// operations about a array
import javafx.scene.layout.Priority;

import java.util.*;

public class ArrayZYH {
    //    public ArrayZYH(){}
    public void selection(int[] unsorted) {
        if (unsorted == null || unsorted.length <= 1) {
            System.out.println("None Input Array.");
        }
        for (int i = 0; i < unsorted.length - 1; i ++) {
            int idx = i;
            for(int j = i + 1; j < unsorted.length; j ++) {
                if(unsorted[idx] > unsorted[j]) {
                    idx = j;
                }
            }
            swap(unsorted, i, idx);
        }
    }
    private void swap(int[] array, int i, int j) {
        int tem = array[i];
        array[i] = array[j];
        array[j] = tem;
    }
    public void mergesort(int[] array, int[] helper, int left, int right) {
        if (left >= right) {
            return;
        }
        int mid = left + (right - left) / 2;
        mergesort(array, helper, left, mid);
        mergesort(array, helper,mid + 1, right);
        merge(array, helper, left, mid, right);
    }
    private void merge(int[] array, int[] helper, int left, int mid, int right) {
        // copy
        for (int i = left; i <= right; i ++) {
            helper[i] = array[i];
        }
        int i = left;
        int j = mid + 1;
        // sort
        while (i <= mid && j <= right) {
            if (helper[i] > helper[j]) {
                array[left ++] = helper[i ++];
            } else {
                array[left ++] = helper[j ++];
            }
        }
        // if i haven't arrive mid
        while (i <= mid) {
            array[left ++] = helper[i ++];
        }
        // if j haven't arrive right, no operation needed
    }
    public void quicksort(int[] array, int left, int right) {
        if (left >= right) {
            return;
        }
        int i = left;
        int j = right - 1;
        while(i <= j) {
            if(array[i] < array[right]) {
                i ++;
            } else {
                swap(array, i, j);
                j --;
            }
        }
        swap(array, right, i);
        quicksort(array, left, i - 1);
        quicksort(array, i + 1, right);
    }
    public int binarySearch(int[] array, int target) {
        if (array == null || array.length == 0) {
            System.out.println("Invalid array!");
            return -1;
        }
        int left = 0;
        int right = array.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (target > array[mid]) {
                left = mid + 1;
            } else if (target < array[mid]) {
                right = mid - 1;
            } else {
                return mid;
            }
        }
        System.out.println("Can't find target number!");
        return -1;
    }
    public int closetSearch(int[] array, int target) {
        if (array == null || array.length == 0) {
            System.out.println("Invalid array");
            return -1;
        }
        int left = 0;
        int right = array.length - 1;
        int idx = 0;
        while(left <= right) {
            int mid = left + (right - left) / 2;
            if (Math.abs(array[mid] - target) < Math.abs(array[idx] - target)) {
                idx = mid;
            }
            if (target == array[mid]) {
                return mid;
            } else if (target > array[mid]) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return idx;
    }
    public int firstOccur(int[] array, int target) {
        if(array == null || array.length == 0) {
            System.out.println("Invalid array!");
            return -1;
        }
        int left = 0;
        int right = array.length - 1;
        while (left <= right) {
            int mid = left + (right - left) / 2;
            if (array[mid] == target) {
                if (mid == 0 || array[mid - 1] != target) {
                    return mid;
                } else {
                    right = mid - 1;
                }
            } else if (array[mid] > target) {
                right = mid - 1;
            } else {
                left = mid + 1;
            }
        }
        System.out.println("Can't find target");
        return -1;
    }
    // assuming k >= 1
    public int[] kCloset(int[] array, int target, int k) {
        if (array == null || array.length < k) {
            System.out.println("Invalid array!");
            return null;
        }
        int closet = closetSearch(array, target);
        int i = closet;
        int j = closet;
        while(j - i + 1 < k) {
            if(i > 0 && j < array.length - 1) {
                if(target - array[i - 1] > array[j + 1] - target){
                    j ++;
                }else{
                    i --;
                }
            }else if(i == 0){
                // j = j + k - (j - i + 1);
                j = k + i - 1;
                break;
            }else{
                // i = i - (k - (j - i + 1));
                i = - k + j + 1;
                break;
            }
        }
        return Arrays.copyOfRange(array, i, j + 1);
    }
    public void min(int[] array){
        StackZYH original = new StackZYH();
        StackZYH minimum = new StackZYH();
        for(int i = 0; i < array.length; i ++){
            original.push(array[i]);
            if(minimum.size() == 0 || minimum.get(minimum.size() - 1) > array[i]){
                minimum.push(array[i]);
            }else{
                minimum.push(minimum.get(minimum.size() - 1));
            }
        }
        minimum.listPrint();
    }
    public void sort(int[] array){
        StackZYH original = new StackZYH();
        StackZYH sorted = new StackZYH();
        for(int i = 0; i < array.length; i ++){
            original.push(array[i]);
        }
        while(original.size() > 0){
            int originalsize = original.size();
            int max = original.get(originalsize - 1);
            int repli = 0;
            int temp;
            while(original.size() > 0){
                temp = original.pop();
                if(temp > max){
                    max = temp;
                    repli = 1;
                }else if(temp == max){
                    repli ++;
                }
                sorted.push(temp);
            }
            for(int i = 0; i < originalsize; i ++){
                temp = sorted.pop();
                if(temp != max){
                    original.push(temp);
                }
            }
            for(int j = 0; j < repli; j ++){
                sorted.push(max);
            }
        }
        sorted.listPrint();
    }
    public int[] kmin(int[] array, int k){
        if(array == null || array.length == 0 || k <= 0){
            return new int[0];
        }else if(array.length <= k){
            return array;
        }
        PriorityQueue<Integer> pq = new PriorityQueue<Integer>(array.length, Collections.reverseOrder());
        for(int i =0; i < array.length; i ++){
            pq.offer(array[i]);
        }
        int[] result = new int[k];
        for(int i = 0; i < k; i++){
            result[i] = pq.poll();
        }
        return result;
    }
    public int[] kmin1(int[] array, int k){
        if(array == null || array.length == 0 || k <= 0){
            return new int[0];
        }else if(array.length <= k){
            return array;
        }
        PriorityQueue<Integer> pq = new PriorityQueue<Integer>(k, Collections.reverseOrder());
        for(int i = 0; i < array.length; i ++){
            if(i < k){
                pq.offer(array[i]);
            }else if(pq.peek() > array[i]){
                pq.poll();
                pq.offer(array[i]);
            }
        }
        int[] result = new int[k];
        for(int i = k - 1; i >= 0; i --){
            result[i] = pq.poll();
        }
        return result;
    }
}
