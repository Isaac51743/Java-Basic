// a new container
public class QueueZYH {
    private StackZYH left;
    private StackZYH right;
    public QueueZYH(){
        left = new StackZYH();
        right = new StackZYH();
    }
    public void push(Integer num){
        left.push(num);
    }
    public Integer pop(){
        if(right.size() == 0){
            Integer temp;
            while(left.size() > 0) {
                temp = left.pop();
                right.push(temp);
            }
        }
        return right.pop();
    }
    public void printQueue(){
        for(int i = left.size() - 1; i >= 0; i --){
            System.out.print(left.get(i) + " ");
        }
        for(int i = 0; i < right.size(); i ++){
            System.out.print(right.get(i) + " ");
        }
        System.out.println();
    }
}
