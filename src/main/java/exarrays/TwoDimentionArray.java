package exarrays;

public class TwoDimentionArray {

    public static void main(String[] args) {
        int[][] intArray1 = new int[3][3];

        intArray1[0][0]  = 1;
        intArray1[0][1]  = 2;
        intArray1[0][2]  = 3;
        intArray1[1][0]  = 4;
        intArray1[1][1]  = 5;
        intArray1[1][2]  = 6;
        intArray1[2][0]  = 7;
        intArray1[2][1]  = 8;
        intArray1[2][2]  = 9;

        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                /*if(i==j){
                    System.out.print(intArray1[i][j]);
                }
                System.out.print('\n');
                if(i<j){
                    System.out.print(intArray1[i][j]);
                }
                if(i>j){
                    System.out.print(intArray1[i][j]);
                }*/
               // System.out.print(intArray1[i][j]);
               // System.out.print('\t');
                System.out.print(intArray1[i][j]+"\t");
            }
            System.out.print('\n');
        }





        Student[][] studentArray = new Student[3][3];

        studentArray[0][0]  = new Student("kavi",56);
        studentArray[0][1]  =  new Student("bavi",56);
        studentArray[0][2]  =  new Student("savi",56);
        studentArray[1][0]  =  new Student("ravi",56);
        studentArray[1][1]  =  new Student("tavi",56);
        studentArray[1][2]  =  new Student("aavi",56);
        studentArray[2][0]  =  new Student("uavi",56);
        studentArray[2][1]  =  new Student("vavi",56);
        studentArray[2][2]  =  new Student("xavi",56);
        for(int i=0;i<3;i++){
            for(int j=0;j<3;j++){
                System.out.print(studentArray[i][j].name+studentArray[i][j].age+"\t");
            }
            System.out.print('\n');
        }


    }
}
