package exarrays;

import java.util.Arrays;

public class RemovingDuplicatesInArray {


    public static void main(String args[]) {
        int[] originalArray = new int[]{1, 1, 2, 2, 3, 3, 4, 5, 5, 6, 7,6};
       quickSort(originalArray, 0, originalArray.length - 1);

        System.out.println("Array with duplicates:" + originalArray);
        removeDuplicates(originalArray);


    }

    public static int[] removeDuplicates(int[] originalArray) {
        // if array length is one, return the array
        if (originalArray.length <= 1) {
            return originalArray;
        }
        int[] uniqueArray = new int[originalArray.length];
        uniqueArray[0] = originalArray[0];
        int lastFound = originalArray[0];
        int totalDuplicates = 0;
        int currentPosition = 1;

        for (int i = 0; i < originalArray.length; i++) {
            if (lastFound == originalArray[i]) {
                totalDuplicates++;
            } else {
                lastFound = originalArray[i];
                uniqueArray[currentPosition] = originalArray[i];
                currentPosition++;
            }
        }

        // at this point array wil have unique numbers along with empty
        // slots at the end in array, lets remove those.

        int newLength = originalArray.length - totalDuplicates;
        uniqueArray = Arrays.copyOf(uniqueArray, newLength + 1);

        for (int i = 0; i < uniqueArray.length; i++) {
            System.out.println("Array with unquie:" + uniqueArray[i]);

        }
        return uniqueArray;
    }


    private static void quickSort(int[] arr, int s, int e) {

        if (s < e) {
            int pivot = findPivot(arr, s, e);
            //System.out.println(pivot);
            // sort left
            quickSort(arr, s, pivot - 1);
            // sort right
            quickSort(arr, pivot + 1, e);
        }

    }

    private static int findPivot(int[] arr, int s, int e) {
        int p = arr[e];
        int i = s;
        for (int j = s; j < e; j++) {
            if (arr[j] <= p) {
                swap(arr, i, j);
                i = i + 1;
            }
        }
        swap(arr, e, i);
        return i;
    }

    private static void swap(int[] arr, int i, int j) {
        int t = arr[i];
        arr[i] = arr[j];
        arr[j] = t;
    }

}
