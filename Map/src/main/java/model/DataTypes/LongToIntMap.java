package model.DataTypes;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Simple Map for primitive type long to int, since Java doesn't support primitive generics.
 */
public class LongToIntMap implements Iterable<Long>, Serializable{
    private int size; // how many KV pairs are in the map 
    private int capacity; // how large are the arrays 
    private int[] values;
    private long[] keys;  
    private boolean[] hasKey; // should take up very little space if the compiler is nice, since booleans only need 1 bit

    /**
     * Constructor for the map
     */
    public LongToIntMap() {
        this.size = 0; 
        this.capacity = 16; // start capacity because then hopefully the compiler just allocates one big block of booleans, taking 8 bytes total(16 w. overhead)
        this.values = new int[capacity]; 
        this.keys = new long[capacity]; 
        this.hasKey = new boolean[capacity]; 
        Arrays.fill(hasKey, false);
    }

    /**
     * Getting the integer corresponding to the specified long
     * @param key the long for which to get the integer
     * @return
     */
    public int get(long key) {
        int hashedKey = hash(key); 

        hashedKey = getPositionOfKey(key, hashedKey);

        return values[hashedKey];
    }

    /**
     * Puts the integer value at the long key
     * @param key the key
     * @param value the value to put at the location of the key
     */
    public void put(long key, int value) {
        if(++size >= capacity / 2) { resize(capacity * 2); }
        int hashedKey = hash(key); 

        hashedKey = getPositionOfKey(key, hashedKey);
        keys[hashedKey] = key; 
        values[hashedKey] = value; 
        hasKey[hashedKey] = true; 
    }

    /**
     * Updates the value at the given key
     * @param key the key at which to replace the value
     * @param value the new value to replace the old one @key
     */
    public void replace(long key, int value) {
        int hashedKey = hash(key); 

        if(!hasKey[hashedKey]) { System.out.println("Tried to replace a nonexistant pair: " + key + ", " + value); return; }
        hashedKey = getPositionOfKey(key, hashedKey); 

        values[hashedKey] = value; 
    }

    /**
     * Removes the specified key
     * @param key the key to remove
     */
    public void remove(long key) {
        int hashedKey = hash(key); 

        if(!hasKey[hashedKey]) { return; }
        hashedKey = getPositionOfKey(key, hashedKey); 

        hasKey[hashedKey] = false; 
        hashedKey++; 
        while(hasKey[hashedKey]) {
            long k = keys[hashedKey];
            int v = values[hashedKey]; 
            hasKey[hashedKey] = false; 
            size--; 
            put(k,v); 
            hashedKey++;  
            if(hashedKey == capacity) { hashedKey = 0; }
        }
        size--; 
    }

    /**
     * Checks if the Map contains the key
     * @param key the key for which to check if contained
     * @return true if contained, false if not
     */
    public boolean containsKey(long key) {
        int hashedKey = hash(key); 
        if(hasKey[hashedKey]) {
            int it = hashedKey; 
            do {
                if(keys[it] == key) { return true; }
                it++; 
                if(it == capacity) { it = 0; } // we obviously also need to wrap here
            } while (hasKey[it]);
        }
        return false; 
    }

    /**
     * Finds the position in the values array for the specified key
     * @param key the key to get the position for
     * @param hashedKey the hashed key
     * @return the index in the values array
     */
    private int getPositionOfKey(long key, int hashedKey) {
        int indexPosition = hashedKey;
        while(hasKey[indexPosition] && keys[indexPosition] != key) {
            indexPosition++;
            if(indexPosition == capacity) { indexPosition = 0; }
        }
        return indexPosition;
    }

    /**
     * Will has the given key
     * @param keyToHash the key to hash
     * @return the hash code for the key
     */
    private int hash(long keyToHash) {
        return (Long.hashCode(keyToHash) & 0x7fffffff) % capacity; // & 0x7fffffff is a cute little bitmask to ensure a positive value  
    }

    /**
     * Getting the size of elements in the Map
     * @return the size of elements
     */
    public int size() { return size; }

    /**
     * Responsible for resizing
     * @param newSize the new size
     */
    private void resize(int newSize) {
        boolean[] oldHasKey = hasKey; 
        hasKey = new boolean[newSize]; 
        Arrays.fill(hasKey, false);
        int[] oldValues = values; 
        values = new int[newSize]; 
        long[] oldKeys = keys; 
        keys = new long[newSize]; 

        int oldCapacity = capacity; 
        capacity = newSize;
        size = 0; 

        for(int i = 0; i < oldCapacity; i++) {
            if(oldHasKey[i]) {
                put(oldKeys[i], oldValues[i]); 
            }
        }
    }

    /**
     * Our custom iterator
     */
    private static class CustomIterator implements Iterator<Long> {
        private int size; 
        private int index; 
        private final long[] arr;

        /**
         * Constructor for the iterator
         * @param hasKey the indexes that are in the Map
         * @param keys the keys in the Map
         * @param size the size of the Map
         */
        public CustomIterator(boolean[] hasKey, long[] keys, int size) {
            this.size = size+1; 
            this.index = 0; 
            this.arr = new long[this.size]; 

            for(int i = 0; i < hasKey.length; i++) {
                if(hasKey[i]){
                    
                    arr[index] = keys[i]; 
                    index++; 
                }
            }
            this.size = index; 
            index = 0; 
        }

        @Override
        public boolean hasNext() {
            //System.out.println(index + "<" + size + " == " + (index < size));
            return index < size;
        }

        @Override
        public Long next() {
            return arr[index++];
        }
        
    }


    @Override
    public Iterator<Long> iterator() {
        return new CustomIterator(hasKey, keys, size);
    }
}
