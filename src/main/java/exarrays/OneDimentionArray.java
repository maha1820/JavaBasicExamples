package exarrays;

public class OneDimentionArray {

    public static void main(String[] args) {

        int[] intarray = new int[3];
        intarray[0] = 1;
        intarray[1] = 2;
        intarray[2] = 3;
        for (int i = 0; i < 3; i++) {
            System.out.println(intarray[i]);
        }
        //objects
        Student[] students = new Student[4];
        students[0] = new Student("ramu", 36);
        students[1] = new Student("samu", 26);
        students[2] = new Student("mamu", 56);
        students[3] = new Student("namu", 16);

        for (int i = 0; i < 4; i++) {
            System.out.println(students[i].name+'\t'+students[i].age);
        }
    }


}
