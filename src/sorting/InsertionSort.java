package sorting;

public class InsertionSort {
    public static void insertionSort(int[] arr, int low, int high) {
        for (int i = low + 1; i <= high; i++) {
            for (int j = i; j > low; j--) {
                if (arr[j] < arr[j - 1]) {
                    exch(arr, j, j - 1);
                } else break;
            }
        }
    }
    public static void exch(int[] a, int i, int j){
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }
}