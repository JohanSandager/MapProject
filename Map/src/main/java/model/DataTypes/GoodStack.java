package model.DataTypes;

import java.util.Iterator;

/**
 * Necessary because Javas iterable stack is not in stack order
 * */
public class GoodStack<T> implements Iterable<T>{
    private Node<T> next;

    /**
     * A node for the
     */
    private static class Node<T> {
        public T val; 
        public Node<T> next;

        /**
         * @param val value of the node
         * @param next pointer to the next node
         */
        public Node(T val, Node<T> next) {
            this.val = val; 
            this.next = next; 
        }
    }

    /**
     * Consturctor for the stack
     */
    public GoodStack() {
        int size = 0;
    }

    /**
     * Pushes a value
     * @param value the value to push
     */
    public void push(T value) {
        next = new Node<>(value, next);
    }

    /**
     * Removes the most recently added element
     * @return the removed element
     */
    public T pop() {
        T retVal = next.val; 
        next = next.next; 
        return retVal; 
    }

    /**
     * Looks at the most recently added element
     * @return the most recently added value
     */
    public T peek() {
        return next.val; 
    }

    /**
     * Checks if the node has a pointer to the next node
     * @return true if there is a pointer, and false if null
     */
    public boolean hasNext() {
        return next != null; 
    }

    /**
     * The actual way an iterator for a stack should look
     */
    private class ActualStackIterator implements Iterator<T> {
        private Node<T> curNode = next;

        @Override
        public boolean hasNext() {
            return curNode != null; 
        }

        @Override
        public T next() {
            T retVal = curNode.val; 
            curNode = curNode.next; 
            return retVal;
        }

    }

    @Override
    public Iterator<T> iterator() {
        return new ActualStackIterator(); 
    }
}

    
