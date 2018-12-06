package exarrays;

/**
 * To find union of two sorted arrays, follow the following merge procedure :

 1) Use two index variables i and j, initial values i = 0, j = 0
 2) If arr1[i] is smaller than arr2[j] then print arr1[i] and increment i.
 3) If arr1[i] is greater than arr2[j] then print arr2[j] and increment j.
 4) If both are same then print any of them and increment both i and j.
 5) Print remaining elements of the larger array.

 Time Complexity : O(m + n)



 To find intersection of 2 sorted arrays, follow the below approach :

 1) Use two index variables i and j, initial values i = 0, j = 0
 2) If arr1[i] is smaller than arr2[j] then increment i.
 3) If arr1[i] is greater than arr2[j] then increment j.
 4) If both are same then print any of them and increment both i and j.
 */

public class UnionAndIntersectionOfTwoArrays {

    public static void main(String args[])
    {
        int arr1[] = {1, 2, 4, 5, 6,11,14};
        int arr2[] = {2, 3, 5, 7,8,9,10,11,12};
        int m = arr1.length;
        int n = arr2.length;
        printUnion(arr1, arr2, m, n);
       // printIntersection(arr1, arr2, m, n);
    }

    /* Function prints union of arr1[] and arr2[]
    m is the number of elements in arr1[]
    n is the number of elements in arr2[] */
    static int printUnion(int arr1[], int arr2[], int m, int n)
    {
        int i = 0, j = 0;
        while (i < m && j < n)
        {
            if (arr1[i] < arr2[j])
                System.out.print(arr1[i++]+" ");
            else if (arr2[j] < arr1[i])
                System.out.print(arr2[j++]+" ");
            else
            {
                System.out.print(arr2[j++]+" ");

                i++;
            }
        }

      /* Print remaining elements of
         the larger array */
        while(i < m)
            System.out.print(arr1[i++]+" ");
        while(j < n)
            System.out.print(arr2[j++]+" ");
        return 0;
    }



    /* Function prints Intersection of arr1[] and arr2[]
       m is the number of elements in arr1[]
       n is the number of elements in arr2[] */
    static void printIntersection(int arr1[], int arr2[], int m, int n)
    {
        int i = 0, j = 0;
        while (i < m && j < n)
        {
            if (arr1[i] < arr2[j])
                i++;
            else if (arr2[j] < arr1[i])
                j++;
            else
            {
                System.out.println(arr2[j++]+" ");
                i++;
            }
        }
    }
}
