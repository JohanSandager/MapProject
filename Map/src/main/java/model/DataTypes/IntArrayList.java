package model.DataTypes;

import java.io.Serializable;

/**
 * Simple ArrayList for primitive type int, since Java doesn't support primitive generics.
 */
public class IntArrayList implements Serializable{
    private int[] array; 
    private int size; 
    private int capacity;

    /**
     * Basic constructor for a unspecified initial size
     */
    public IntArrayList() {
        this.size = 0; 
        this.capacity = 4; 
        this.array = new int[capacity]; 
    }

    /**
     * Basic constructor for a specified initial size
     * @param capacity the initial capacity
     */
    public IntArrayList(int capacity) {
        this.size = 0; 
        this.capacity = capacity;
        this.array = new int[this.capacity];
    }

    /**
     * Gets the element at the specified index
     * @param index the index of the requested element
     * @return the element at the index
     */
    public int get(int index) {
        return array[index]; 
    }

    /**
     * Adds a value to the ArrayList
     * @param val the value to add
     */
    public void add(int val) {
        if(size + 1 == array.length) {
            resize(array.length * 2); 
        }
        array[size] = val; 
        size++; 
    }

    /**
     * Finds the index of an element
     * @param val the value to find the index for
     * @return the index of the element if found, if not it returns -1
     */
    public int find(int val) {
        for(int i = 0; i < size; i++) {
            if(array[i] == val) { return i; }
        }
        return -1;
    }

    /**
     * Gets the size of the ArrayList
     * @return the size of elements of the ArrayList
     */
    public int size() { return size; }

    /**
     * Gets the current capacity of the ArrayList
     * @return the current capacity of the ArrayList
     */
    public int curCapacity() { return array.length; }

    /**
     * Removes the element at the specified index
     * @param index the index of the element to delete
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
     * Resizes the array in accordance with the specified new size
     * @param newSize the size to resize to
     */
    private void resize(int newSize) {
        capacity = newSize; 
        int[] newArr = new int[newSize]; 
        for(int i = 0; i < size; i++) {
            newArr[i] = array[i]; 
        }
        array = newArr; 
    }
}
