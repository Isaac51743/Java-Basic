public class DequeueZYH {
    StackZYH left;
    StackZYH right;
    StackZYH temp;
    public DequeueZYH(){
        left = new StackZYH();
        right = new StackZYH();
        temp = new StackZYH();
    }
    public void pushL(Integer num){
        left.push(num);
    }
    public void pushR(Integer num){
        right.push(num);
    }
    public Integer popL(){
        if(left.size() == 0){
            if(right.size() == 0){
                return null;
            }else{
                int half = right.size()/2;
                for(int i = 0; i < half; i ++){
                    temp.push(right.pop());
                }
                while(right.size() > 0){
                    left.push(right.pop());
                }
                while(temp.size() > 0){
                    right.push(temp.pop());
                }
            }
        }
        return left.pop();
    }
    public Integer popR(){
        if(right.size() == 0){
            if(left.size() == 0){
                return null;
            }else{
                int half = left.size()/2;
                for(int i = 0; i < half; i ++){
                    temp.push(left.pop());
                }
                while(left.size() > 0){
                    right.push(left.pop());
                }
                while(temp.size() > 0){
                    left.push(temp.pop());
                }
            }
        }
        return right.pop();
    }
    public void printDequeue(){
        for(int i = left.size() - 1; i >= 0; i --){
            System.out.print(left.get(i) + " ");
        }
        for(int j = 0; j < right.size(); j ++){
            System.out.print(right.get(j) + " ");
        }
        System.out.println();
    }
}
