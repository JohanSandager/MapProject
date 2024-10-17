package model;

import java.util.ArrayList;

/**
 * The ValueChangeSubject class extends Subject.
 * It has some value and notifies all observers when that value changes.
 */
public class ValueChangeSubject<T> extends Subject {
    private T value;

    /**
     * Constructor for the ValueChangeSubject class. Takes a value and sets this.value to value.
     * It also initializes observers in a new ArrayList.
     * @param value, the value of the subject that can change
     */
    public ValueChangeSubject(T value) {
        this.value = value; 
        observers = new ArrayList<>(); 
    }

    /**
     * Override constructor for the ValueChangeSubject class. Takes a value and sets this.value to value.
     * Initializes observers to new ArrayList
     */
    public ValueChangeSubject() {
        observers = new ArrayList<>(); 
    }

    /**
     * void method setValue for setting the value to a new value and notifying observers
     * @param newValue the value that needs to be set.
     */
    public void setValue(T newValue) { value = newValue; notifyObservers(); }

    /**
     * void method getValue for setting the value to a new value and notifying observers
     * @return returns the value
     */
    public T getValue() { return value; }
}