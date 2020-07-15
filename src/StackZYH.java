// a new container
import java.util.*;
public class StackZYH {
    private List<Integer> basic;
    public StackZYH(){
        basic = new LinkedList<Integer>();
    }
    public int size(){
        return basic.size();
    }
    public Integer pop(){
        if(basic.size() == 0){
            return null;
        }
        Integer temp = basic.get(basic.size() - 1);
        basic.remove(basic.size() - 1);
        return temp;
    }
    public void push(Integer num){
        basic.add(num);
    }
    public void listPrint(){
        for(int i = 0; i < basic.size(); i ++){
            System.out.print(basic.get(i) + " ");
        }
        System.out.println();
    }
    public Integer get(int idx){
        return basic.get(idx);
    }
}

