package exarrays;

public class DynamicArrayDemo {

    public static void main(String args[]){

        DynamicArray<Integer> dynamicArray = new DynamicArray();

        dynamicArray.put(1);
        System.out.println("Size:"+dynamicArray.getSize());
        dynamicArray.put(20);
        System.out.println("Size:"+dynamicArray.getSize());
        dynamicArray.put(30);
        System.out.println("Size:"+dynamicArray.getSize());
        dynamicArray.put(40);
        System.out.println("Size:"+dynamicArray.getSize());

        for (int i = 0; i < dynamicArray.getSize(); i++) {
            System.out.println(dynamicArray.get(i));
        }
    }
}
