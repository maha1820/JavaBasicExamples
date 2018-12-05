package exarrays;

public class LargestAndSmallestNumberInUnsortedArray {
    public static void main(String args[]){
        int [] intArray  = new int[] {2,8,9,10,2,4,5,11};
        minAndMaxValues(intArray);

    }


    public static void minAndMaxValues(int [] intArray ){

        int minVal=intArray[0];
        int maxVal=intArray[0];


        for(int i=0;i<intArray.length;i++){
            if(intArray[i]<minVal){
                minVal= intArray[i];
            }
            if(intArray[i]>maxVal){
                maxVal=intArray[i];
            }
        }

        System.out.println("minVal:    "+minVal+"   maxVal:    "+maxVal);

    }
}
