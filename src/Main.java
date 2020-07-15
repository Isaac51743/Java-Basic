import java.util.*;
import java.lang.Math;
public class Main {
    private static void printIntArray(int[] array){
        for(int element: array){
            System.out.print(element + " ");
        }
        System.out.println();
    }
    private static void printLinkList(ListNode head){
        while(head != null){
            System.out.print(head.value + " -> ");
            head = head.next;
        }
        System.out.println("null");
    }
    private static void printStringArray(char[] array){
        for(char element: array){
            System.out.print(element + " ");
        }
        System.out.println();
    }
    private static int fibonacci(int n){
        if(n == 0 || n == 1){
            return n;
        }
        return fibonacci(n - 1) + fibonacci(n - 2);
    }
    private static double aPowerB(int a, int b){
        if(a == 0 && b <= 0){
            System.out.println("error a or b");
            return -1;
        }else if(a == 0 && b > 0){
            return 0;
        }else if(a != 0 && b >= 0){
            if(b == 0){
                return 1;
            }else if(b == 1){
                return a;
            }
            double temp = aPowerB( a, b/2);
            if(b % 2 == 0){
                return  temp * temp;
            }else{
                return temp * temp * a;
            }
        }else{
            return 1.0 / aPowerB(a, -b);
        }
    }
    public static void main(String[] args) {
        // C1 Array
        System.out.println("Array:");
        int[] unsorted = new int[] {2, 4, 4, 4, 12, 4, 10};
        ArrayZYH tool1 = new ArrayZYH();
        // selection sort
        tool1.selection(unsorted);
        System.out.print("selection sort: ");
        printIntArray(unsorted);
        // mergesort
        int[] helper = new int[unsorted.length];
        tool1.mergesort(unsorted, helper, 0, unsorted.length - 1);
        System.out.print("mergesort ");
        printIntArray(unsorted);
        tool1.quicksort(unsorted, 0, unsorted.length - 1);
        System.out.print("quicksort: ");
        printIntArray(unsorted);
        System.out.println("fibonacci: " + fibonacci(3));
        System.out.println("a to the power of b: " + aPowerB(3, -1));
        System.out.println("binary search index: " + tool1.binarySearch(unsorted, 4));
        System.out.println("binary search closet's index: " + tool1.closetSearch(unsorted, 9));
        System.out.println("index of first occurance: " + tool1.firstOccur(unsorted, 4));
        System.out.print("k closet: ");
        printIntArray(tool1.kCloset(unsorted, 2, 3));
        printIntArray(unsorted);
        tool1.min(unsorted);
        tool1.sort(unsorted);

        // C2 linked list
        System.out.println();
        System.out.println("Linked List:");
        LinkListZYH tool2 = new LinkListZYH();
        ListNode testhead = tool2.arrar2LinkedList(new int[] {0, 1, 2, 3, 4});
        // 0 -> 1 -> 2 -> 3 -> 4

        printLinkList(testhead);
        System.out.println("get length: " + tool2.getLength(testhead));
        System.out.println("get index: " + tool2.index(testhead, 3).value);
        ListNode h1 = tool2.appendHead(testhead, 10);
        printLinkList(h1);
        ListNode h2 = tool2.appendTail(h1, -2);
        printLinkList(h2);
        ListNode h3 = tool2.removeElement(h2, 3);
        printLinkList(h3);
        ListNode h4 = tool2.reverse(h3);
        printLinkList(h4);
        ListNode h5 = tool2.reverseRecursion(h4);
        printLinkList(h5);
        System.out.println(tool2.findmid(h5).value);
        System.out.println(tool2.findcircle(h5));
        printLinkList(tool2.insert(testhead.next, 3));
        ListNode head1 = tool2.arrar2LinkedList(new int[] {2, 4, 5, 6, 8});
        ListNode head2 = tool2.arrar2LinkedList(new int[] {1, 4, 5, 7, 8});
        printLinkList(tool2.merge(head1, head2));
        System.out.println();

        // C3 queue
        System.out.println("Queue:");
        QueueZYH q = new QueueZYH();
        for(int i = unsorted.length - 1; i >= 0; i --){
            q.push(unsorted[i]);
        }
        for(int j = 0; j < 3; j ++){
            q.pop();
        }
        printIntArray(unsorted);
        q.printQueue();

        // C4 Dequeue
        DequeueZYH d = new DequeueZYH();
        for(int i = unsorted.length - 1; i >= 0; i --){
            d.pushL(unsorted[i]);
        }
        for(int j = 0; j < 3; j ++){
            d.popR();
        }
        d.printDequeue();
        System.out.println();

        // C5 heap
        System.out.println("Heap:");
        printIntArray(unsorted);
        printIntArray(tool1.kmin1(unsorted, 3));
        HeapZYH hp = new HeapZYH(10);
        for(int i = 0; i < 10; i ++){
            hp.offer(10 - i);
        }
        hp.printheap();

        String a = "haha";
        System.out.println(a instanceof String);
        Map hashMap = new HashMap<TreeNode, Integer>();
        hashMap.put(new TreeNode(2), 3);
        hashMap.put(new TreeNode(2), 4);
        System.out.println(hashMap);

    }

}
