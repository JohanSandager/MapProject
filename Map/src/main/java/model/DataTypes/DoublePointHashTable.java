package model.DataTypes;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A hashtable implementation for twodimensional points with unique integer ID's. 
 * A point can be retrieved in constant time via it's ID, and an ID can be retried from 
 * a point's coordinates in ~constant time(hashing dependent). 
 * Dynamically resizes itself, maintaining a load between 25% and 50%. Does not support removals, since they are unneeded at the client. 
 * */
public class DoublePointHashTable implements Serializable {
    private double[] xCoords;
    private double[] yCoords; 
    private int[] revIndices; 
    private int[] idIndices; 
    private int capacity; 
    private int size;

    /**
     * Basic constructor for the HashTable without a specified initial capacity
     * */
    public DoublePointHashTable() {
        this(64);
    }

    /**
     * Basic constructor for the HashTable with a specified initial capacity
     * @param capacity initial capacity
     * */
    public DoublePointHashTable(int capacity) {
        this.size = 0; 
        this.capacity = capacity;
        this.xCoords = new double[this.capacity];
        this.yCoords = new double[this.capacity];
        this.revIndices = new int[this.capacity];
        this.idIndices = new int[this.capacity /2 + 1];
        Arrays.fill(this.revIndices, -1); 
    }

    /**
     * Adds the double point to the HashTable
     * @param x x coordinate
     * @param y y coordinate
     * @return the index where the point was added
     * */
    public int add(double x, double y) {
        if(size+1 >= capacity / 2 ) { resize(capacity*2); }
        rawAdd(x, y, size);
        return size-1; 
    }

    /**
     * @param x the x coordinate
     * @param y the y coordinate
     * @param id the id
     */
    private void rawAdd(double x, double y, int id) {
        int hashCode = hash(x,y); 

        hashCode = linearProbing(hashCode);

        idIndices[id] = hashCode; 
        revIndices[hashCode] = id; 
        xCoords[hashCode] = x; 
        yCoords[hashCode] = y; 

        size++; 
    }

    /**
     * Finds the index in the idIndices array of the point
     * @param x x coordinate
     * @param y y coordinate
     * @return the index as an int
     */
    public int findID(double x, double y) {
        int hashCode = hash(x,y); 

        while(revIndices[hashCode] != -1 ) {
            if(DoubleArrayList.doubleEquals(x, xCoords[hashCode]) && DoubleArrayList.doubleEquals(y, yCoords[hashCode])) {
                return revIndices[hashCode]; 
            }
            hashCode++; 
            if(hashCode == capacity) { hashCode = 0;}
        }

        return -1; 
    }

    /**
     * Returns the x coordinate for the given id
     * @param id the id to find the x coordinate for
     * @return the x coordinate
     */
    public double getXFromID(int id) {
        return xCoords[idIndices[id]]; 
    }

    /**
     * Returns the y coordinate for the given id
     * @param id the id to find the y coordinate for
     * @return the y coordinate
     */
    public double getYFromID(int id) {
        return yCoords[idIndices[id]]; 
    }

    /**
     * Gets all the x coordinates
     * @return a DoubleArrayList with all x coordinates
     */
    public DoubleArrayList getXCoordsList() {
        return createArrayList(xCoords); 
    }

    /**
     * Gets all the y coordinates
     * @return a DoubleArrayList with all y coordinates
     */
    public DoubleArrayList getYCoordsList() {
        return createArrayList(yCoords); 
    }

    /**
     * Creates an ArrayList of primitive type double
     * @param arrayToCopy the array to be copied
     * @return a DoubleArrayList with the copied elements
     */
    private DoubleArrayList createArrayList(double[] arrayToCopy) {
        DoubleArrayList coordinateList = new DoubleArrayList(size);
        for(int i = 0; i < size; i++) {
            coordinateList.add(arrayToCopy[idIndices[i]]);
        } 
        return coordinateList;
    }


    /**
     * Retrieves the index of the given key
     * @param initialIndex the initial index to be looked up
     * @return the index of the first available position
     */
    private int linearProbing(int initialIndex) {
        int availableIndex = initialIndex;
        while(revIndices[availableIndex] != -1) {
            availableIndex++;
            if(availableIndex == capacity) { availableIndex = 0;}
        }
        return availableIndex;
    }

    /**
     * Gets the size of elements in the HashTable
     * @return the size of elements
     */
    public int size() { return size; }

    /**
     * Responsible for resizing the HashTable
     * @param newSize the new size of the HashTable
     */
    private void resize(int newSize) {
        double[] oXCoords = xCoords; 
        xCoords = new double[newSize]; 
        double[] oYCoords = yCoords; 
        yCoords = new double[newSize]; 
        int[] oRevIndices = revIndices; 
        revIndices = new int[newSize];
        //int[] oIdIndices = idIndices; 
        idIndices = new int[newSize / 2 + 1]; 

        Arrays.fill(revIndices, -1); 

        int oldCapacity = capacity; 
        capacity = newSize; 
        size = 0; 

        for(int i = 0; i < oldCapacity; i++) {
            if(oRevIndices[i] != -1) {
                rawAdd(oXCoords[i], oYCoords[i], oRevIndices[i]); 
            }
        }
    }

    /**
     * Gets the hash code of the point
     * @param x x coordinate
     * @param y y coordinate
     * @return the hash code of the point
     */
    private int hash(double x, double y) {
        return (Double.hashCode(x * y) & 0x7fffffff)  % capacity; //0x7fffffff is same as Math.abs() but faster
    }
}
