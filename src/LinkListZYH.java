// operation about a Linked list
public class LinkListZYH {
    public int getLength(ListNode head){
        ListNode cur = head;
        int length = 0;
        while(cur != null){
            length ++;
            cur = cur.next;
        }
        return length;
    }
    public ListNode index(ListNode head, int idx){
        if(head == null){
            System.out.println("invalid head");
            return null;
        }
        if(idx < 0){
            System.out.println("invalid index");
            return null;
        }
        ListNode cur = head;
        while(idx > 0 && cur != null){
            cur = cur.next;
            idx --;
        }
        return cur;
    }
    public ListNode appendHead(ListNode head, int value){
        ListNode tem = new ListNode(value);
        tem.next = head;
        return tem;
    }
    public ListNode appendTail(ListNode head, int value){
        if(head == null){
            return new ListNode(value);
        }
        ListNode cur = head;
        while(cur.next != null){
            cur = cur.next;
        }
        cur.next = new ListNode(value);
        return head;
    }
    // assuming no duplication
    public ListNode removeElement(ListNode head, int val){
        if(head == null) {
            return null;
        }
        if(head.value == val){
            return head.next;
        }
        ListNode cur = head;
        while(cur.next != null && cur.next.value != val){
            cur = cur.next;
        }
        if(cur.next != null){
            cur.next = cur.next.next;
        }
        return head;
    }
    public ListNode reverse(ListNode head){
        if(head == null){
            return null;
        }
        ListNode pre = null;
        ListNode cur = head;
        ListNode next;
        while(cur != null){
            next = cur.next;
            cur.next = pre;
            pre = cur;
            cur = next;
        }
        return pre;
    }
    public ListNode reverseRecursion(ListNode head){
        if(head == null || head.next == null){
            return head;
        }
        ListNode newhead = reverseRecursion(head.next);
        head.next.next = head;
        head.next = null;
        return newhead;
    }
    public ListNode findmid(ListNode head){
        if(head == null){
            return null;
        }
        ListNode slow = head;
        ListNode fast = head;
        while(fast.next != null && fast.next.next != null){
            slow = slow.next;
            fast = fast.next.next;
        }
        return slow;
    }
    public boolean findcircle(ListNode head){
        if(head == null){
            return false;
        }
        ListNode slow = head;
        ListNode fast = head;
        while(fast.next != null && fast.next.next != null){
            slow = slow.next;
            fast = fast.next.next;
            if(slow == fast){
                return true;
            }
        }
        return false;
    }
    public ListNode insert(ListNode head, int val){
        if(head == null){
            return new ListNode(val);
        }
        ListNode cur = head;
        ListNode pre = null;
        while(cur.value <= val){
            pre = cur;
            cur = cur.next;
        }
        if(cur == head){
            ListNode newhead = new ListNode(val);
            newhead.next = head;
            return newhead;
        }else if(cur == null){
            pre.next = new ListNode(val);
        }else{
            pre.next = new ListNode(val);
            pre.next.next = cur;
        }
        return head;
    }
    public ListNode merge(ListNode h1, ListNode h2){
        ListNode dummy = new ListNode(0);
        ListNode cur1 = h1;
        ListNode cur2 = h2;
        ListNode end = dummy;
        while(cur1 != null && cur2 != null){
            if(cur1.value > cur2.value){
                end.next = cur2;
                cur2 = cur2.next;
            }else{
                end.next = cur1;
                cur1 = cur1.next;
            }
            end = end.next;
        }
        if(cur1 == null){
            end.next = cur2;
        }else{
            end.next = cur1;
        }
        return dummy.next;
    }
    public ListNode arrar2LinkedList(int[] array){
        if(array == null){
            return null;
        }
        ListNode dummy = new ListNode(0);
        ListNode cur = dummy;
        for(int i = 0; i < array.length; i ++){
            cur.next = new ListNode(array[i]);
            cur = cur.next;
        }
        return dummy.next;
    }
}
