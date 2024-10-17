package model.DataTypes;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A datastructure for the primitive type double
 * */
public class DoubleIndexMinPQ implements Serializable {
    private final double[] values; // heap ordered double values
    private final int[] indices; // indices[x] is the heap order position of x
    private final int[] revIndices;
    private final int capacity;
    private int size;

    /**
     * Basic constructor for the PQ without a specified initial capacity
     * */
    public DoubleIndexMinPQ(){
        this.capacity = 65;
        size = 0;
        indices = new int[capacity];
        revIndices = new int[capacity];
        values = new double[capacity];
        Arrays.fill(indices, -1);
    }

    /**
     * Constructor for the PQ with a specified initial capacity
     * @param capacity the initial capacity
     * */
    public DoubleIndexMinPQ(int capacity){
        this.capacity = ++capacity; 
        size = 0; 
        indices = new int[capacity]; 
        revIndices = new int[capacity]; 
        values = new double[capacity]; 
        Arrays.fill(indices, -1); 
    }

    /**
     * Checks if the queue is empty
     * @return true if the queue is empty, false if not
     * */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Size of elements in the PQ
     * @return the size of the elements as an int
     * */
    public int size() { 
        return size; 
    }

    /**
     * Capacity of elements in the PQ
     * @return the capacity of the elements as an int
     * */
    public int capacity() {
        return capacity; 
    }

    /**
     * Inserts an element if not already in the PQ, if already contained, it will replace it
     * @param  weight the weight of the element
     * @param key the key to insert
     * */
    public void insertOrReplace(double weight, int key) {
        if(contains(key)) replace(key, weight);  
        else insert(weight, key);
    }

    /**
     * Inserts an element into to PQ at the place corresponding to the weight
     * @param weight the weight of the element
     * @param key the key to insert
     * */
    public void insert(double weight, int key) { // inserts this value pairing at the very last spot and then swims it 
        indices[key] = ++size; 
        values[size] = weight; 
        revIndices[size] = key; 
        swim(size); 
    }

    /**
     * Replace the weight of the element with the new weight
     * @param newWeight the new weight of the element
     * @param key the key to insert
     * */
    public void replace(int key, double newWeight) {
        values[indices[key]] = newWeight;
        if(indices[key] * 2 <= size && newWeight > values[indices[key] * 2]) sink(indices[key]); // new weight is bigger than a child
        else if(indices[key] / 2 > 0 && newWeight < values[indices[key] / 2]) swim(indices[key]); // new weight is smaller than a parent
        
    }

    /**
     * Checks if the PQ contains the given key
     * @param key the key to check
     * @return true if contained, false if not
     * */
    public boolean contains(int key) {
        return indices[key] != -1; 
    }

    /**
     * Deletes the smallest element and returns the index
     * @return the index of the deleted element
     * */
    public int delMinIndex() {
        int min = revIndices[1];
        exchange(revIndices[1] , revIndices[size--]);
        sink(1); 
        indices[min] = -1;
        return min;
    }

    /**
     * Peeks at the smallest value
     * */
    public double peekMinValue() {
        return values[1];
    }

    /**
     * Deletes the smallest value and returns it
     * @return smallest value
     * */
    public double delMinValue() {
        int min = revIndices[1];
        double minVal = values[1];
        exchange(revIndices[1] , revIndices[size--]);
        sink(1); 
        indices[min] = -1;
        return minVal;
    }

    /**
     * Exchanges two elements in the PQ
     * @param a the first element
     * @param b the second element
     * */
    private void exchange(int a, int b) {
        double tempVal = values[indices[a]]; 
        int tempRevI = revIndices[indices[a]]; 
        int tempI = indices[a]; 

        values[indices[a]] = values[indices[b]]; 
        revIndices[indices[a]] = revIndices[indices[b]]; 
        indices[a] = indices[b]; 
        
        values[indices[b]] = tempVal; 
        revIndices[indices[b]] = tempRevI; 
        indices[b] = tempI; 
    }

    /**
     * Makes the element at the specified index, swim up to its correct place
     * @param index the index of the element to swim up
     * */
    private void swim(int index) {
        while(index > 0 && values[index] < values[index/2]) { // while the index exists and it's smaller than its parent
            exchange(revIndices[index], revIndices[index / 2]);
            index /= 2; 
        }
    }

    /**
     * Makes the element at the specified index, sink down to its correct place
     * @param index the index of the element to sink down
     * */
    private void sink(int index) {
        while(2 * index <= size) {
            int child = index * 2;
            if(child+1 <= size && values[child] > values[child+1]) child++; // grab the smallest child 
            if(values[child] >= values[index]) break; // by heap order, if index less than its smallest child it's in order
            exchange(revIndices[child], revIndices[index]); // exchange with the smallest child, ensuring this part of the heap is in order
            index = child; 
        }
    }
}
