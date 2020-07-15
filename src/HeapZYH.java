import java.util.Collections;
import java.util.NoSuchElementException;

public class HeapZYH {
    private int[] array;
    private int size;
    public HeapZYH(int[] array){
        if(array == null || array.length == 0){
            throw new IllegalArgumentException("invalid array!");
        }
        this.array = array;
        this.size = array.length;
        heapify();
    }
    private void heapify(){
        for(int i = size/2 - 1; i >= 0; i --){
            percolateDown(i);
        }
    }
    private void swap(int i, int j){
        int tem = array[i];
        array[i] = array[j];
        array[j] = tem;
    }
    private void percolateDown(int idx){
        while(idx <= size/2 - 1){
            int leftchild = idx *2 + 1;
            int rightchild = leftchild + 1;
            int minidx = leftchild;
            if(rightchild < size && array[leftchild] > array[rightchild]){
                minidx = rightchild;
            }
            if(array[idx] > array[minidx]){
                swap(idx, minidx);
                idx = minidx;
            }
            else{
                break;
            }
        }
    }
    private void percolateUp(int idx){
        while(idx > 0){
            int parentidx = (idx - 1)/2;
            if(array[parentidx] > array[idx]){
                swap(parentidx, idx);
                idx = parentidx;
            }else{
                break;
            }
        }
    }
    public boolean isFull(){
        return size == array.length;
    }
    public boolean isEmpty(){
        return size == 0;
    }
    public int size(){
        return size;
    }
    public HeapZYH(int capacity){
        if(capacity <= 0){
            throw new IllegalArgumentException("invalid capacity");
        }
        this.array = new int[capacity];
        this.size = 0;
    }
//    public HeapZYH(int capacity, Collections comparator){
//
//    }
    public int peek(){
        if(size == 0){
            throw new NoSuchElementException("heap is empty");
        }
        return array[0];
    }
    public int poll(){
        if(size == 0){
            throw new NoSuchElementException("heap is empty!");
        }
        int result = array[0];
        array[0] = array[size - 1];
        size --;
        if (size > 0){
        percolateDown(0);
        }
        return result;
    }
    public void offer(int num){
        if(size == array.length){
            int[] newarray = new int[size + size/2];
            copy(this.array, newarray);
            this.array = newarray;
        }
        array[size] = num;
        percolateUp(size);
        size ++;
    }
    private void copy(int[] shortarray, int[] longarray){
        for(int i = 0; i < shortarray.length; i ++){
            longarray[i] = shortarray[i];
        }
    }
    public void printheap(){
        for(int element: this.array){
            System.out.print(element + " ");
        }
        System.out.println();
    }
}
