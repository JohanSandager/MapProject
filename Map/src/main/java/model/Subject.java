package model;

import java.util.List;

/**
 * Public abstract class for subjects utilized in an observer pattern.
 * The class mantains a list of observers and has a notifyObservers() method that will be used for notifying observers when
 * there are changes in the state of the class that the observers observe.
 * There is also an addObserver method that can add new observers.
 */
public abstract class Subject {
    protected List<Observer> observers;
    /**
     * Void method that notifies observers by calling the observers update function
     */
    protected void notifyObservers() {
        for (Observer observer : observers) {
            observer.update();
        }
    }
    /**
     * Void method that adds a new observer to the list of observers.
     * @param obs - the observer to the added
     */
    public void addObserver(Observer obs) { observers.add(obs); }
}

