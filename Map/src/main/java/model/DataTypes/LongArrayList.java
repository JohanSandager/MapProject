package model.DataTypes;

import java.io.Serializable;

/**
 * Simple ArrayList for primitive type long, since Java doesn't support primitive generics.
 */
public class LongArrayList implements Serializable{
    private long[] array; 
    private int size; 
    private int capacity;

    /**
     * Basic constructor for an unspecified initial capacity
     */
    public LongArrayList() {
        this.size = 0; 
        this.capacity = 4; 
        this.array = new long[capacity]; 
    }

    /**
     * Basic constructor for a specified initial capacity
     * @param capacity the initial capacity
     */
    public LongArrayList(int capacity) {
        this.size = 0; 
        this.capacity = capacity;
        this.array = new long[this.capacity];
    }

    /**
     * Getting the element at the specified index
     * @param index the index for the lookup
     * @return the element at the index
     */
    public long get(int index) {
        return array[index]; 
    }

    /**
     * Adding an element to the ArrayList
     * @param val the value to add to the ArrayList
     */
    public void add(long val) {
        if(size + 1 == capacity) {
            resize(2 * capacity); 
        }
        array[size] = val; 
        size++; 
    }

    /**
     * Size of the ArrayList
     * @return size of the ArrayList as an int
     */
    public int size() { return size; }

    /**
     * Capacity of the ArrayList
     * @return current capacity of the ArrayList as an int
     */
    public int curCapacity() { return array.length; }

    /**
     * Removed a specified element
     * @param val
     */
    public void remove(long val) {
        for(int i = 0; i < size; i++) {
            if(array[i] == val) { remove(i); return; }
        }
    }

    /**
     * Removes the element at the specified index
     * @param index the index for which to remove
     */
    public void remove(int index) {
        if(index < --size) { 
            for(int i = index; i < size; i++) { 
                array[i] = array[i+1];
            }
        }
        if(size < array.length / 4) {
            resize(array.length / 2); 
        }
    }

    /**
     * Responsible for resizing the ArrayList
     * @param newSize the new size of the ArrayList
     */
    private void resize(int newSize) {
        capacity = newSize; 
        long[] newArr = new long[newSize]; 
        for(int i = 0; i < size; i++) {
            newArr[i] = array[i]; 
        }
        array = newArr; 
    }
}
