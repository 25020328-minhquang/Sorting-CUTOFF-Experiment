package sorting;
import java.util.Random;

public class QuickSort {
    private static int partition(int[] arr, int low, int high) {
        int i  = low, j = high + 1;
        while (true) {
            while (arr[++i] < arr[low]) {
                if (i == high) break;
            }
            while (arr[--j] > arr[low]) {
                if (j == low) break;
            }
            if (i >= j) break;
            InsertionSort.exch(arr, i, j);
        }
        InsertionSort.exch(arr, low, j);
        return j;
    }
    private static void sort(int[] arr,int low, int high,int CUTOFF) {
        if(high <= low + CUTOFF - 1){
            InsertionSort.insertionSort(arr, low, high);
            return;
        }
        int j = partition(arr, low, high);
        sort(arr, low, j - 1, CUTOFF);
        sort(arr, j + 1, high,CUTOFF);
    }
    public static void quickSort(int[] arr,int CUTOFF){
        shuffle(arr);
        sort(arr, 0, arr.length - 1,CUTOFF);
    }
    private static void shuffle(int[] arr) {
        Random random = new Random();

        for (int i = arr.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);

            InsertionSort.exch(arr, i, j);
        }
    }
}
