package model.DataTypes;

import java.io.Serializable;

/**
 * Simple ArrayList for primitive type double, since Java doesn't support primitive generics.
 * */
public class DoubleArrayList implements Serializable{
    private double[] array; 
    private int size; 
    private int capacity; 

    /**
     * Basic constructor for a undefined initial size
     * */
    public DoubleArrayList() {
        this.size = 0; 
        this.capacity = 4; 
        this.array = new double[this.capacity]; 
    }

    /**
     * Basic constructor for a defined initial size
     * @param initSize initial size of the ArrayList
     * */
    public DoubleArrayList(int initSize) {
        this.capacity = initSize; 
        this.size = 0; 
        this.array = new double[this.capacity]; 
    }


    /**
     * Returning the element for the given index
     * @param index the index for the lookup
     * @return the element at the specified position
     * */
    public double get(int index) {
        return array[index]; 
    }

    /**
     * Adding an element to the ArrayList
     * @param val the value to add to the ArrayList
     * */
    public void add(double val) {
        if(size + 1 == array.length) {
            resize(array.length * 2); 
        }
        array[size] = val; 
        size++; 
    }

    /**
     * Size of the ArrayList
     * @return size of the ArrayList as an int
     * */
    public int size() { return size; }

    /**
     * Capacity of the ArrayList
     * @return current capacity of the ArrayList as an int
     * */
    public int curCapacity() { return array.length; }

    /**
     * Removed a specified element
     * @param val the element to remove
     * */
    public void remove(double val) {
        for(int i = 0; i < size; i++) {
            if(doubleEquals(array[i], val)) { remove(i); return; }
        }
    }

    /**
     * Checks if the ArrayList contains the specified element
     * @param val the element to check if contained
     * @return whether the element is in the ArrayList as a boolean
     * */
    public boolean contains(double val) {
        return find(val) != -1;
    }

    /**
     * Finds the index of the requested value
     * @param val the value to find
     * @return returns the index if found, else it returns -1
     * */
    public int find(double val) {
        for(int i = 0; i < size; i++) {
            if(doubleEquals(array[i], val)) { return i; }
        }
        return -1;
    }

    /**
     * For comparing doubles with a margin of imprecision of 0.0001
     * */
    public static boolean doubleEquals(double val1, double val2) {
        return Math.abs(val1 - val2) < 0.0001; 
    }

    /**
     * Removes the value at the specified index
     * @param index the index to remove
     * */
    public void remove(int index) {
        if(index < --size) { // if this is false it will not incur any problem, since --size means we just overwrite this value on next add operation
            for(int i = index; i < size; i++) { 
                array[i] = array[i+1]; // we cant overflow case we're decreasing size by 1, when we're removing 1 element
            }
        }
        if(size < array.length / 4) {
            resize(array.length / 2); 
        }
    }

    /**
     * The function responsible for resizing the array
     * @param newSize the size to resize to
     * */
    private void resize(int newSize) {
        capacity = newSize; 
        double[] newArr = new double[newSize];
        if (size >= 0) System.arraycopy(array, 0, newArr, 0, size);
        array = newArr; 
    }
}
