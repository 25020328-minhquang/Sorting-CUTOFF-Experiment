package sorting;

public class MergeSort {
    private static void merge(int[] arr,int[] aux, int low, int mid, int high) {
        for(int k = low; k <= high; k++) {
            aux[k] = arr[k];
        }
        int i = low;
        int j = mid + 1;
        for(int k = low; k <= high; k++) {
            if(i > mid) arr[k] = aux[j++];
            else if(j > high) arr[k] = aux[i++];
            else if(aux[i] < aux[j]) arr[k] = aux[i++];
            else arr[k] = aux[j++];
        }
    }
    public static void sort(int[] arr,int[] aux, int low, int high,int CUTOFF) {
        if(high <= low + CUTOFF - 1){
            InsertionSort.insertionSort(arr, low, high);
            return;
        }
        int mid = (low + high)/2;
        sort(arr, aux, low, mid,CUTOFF);
        sort(arr, aux, mid + 1, high,CUTOFF);
        merge(arr, aux, low, mid, high);
    }
    public static void mergeSort(int[] arr,int CUTOFF) {
        int[] aux = new int[arr.length];
        sort(arr, aux, 0, arr.length - 1,CUTOFF);
    }
}
