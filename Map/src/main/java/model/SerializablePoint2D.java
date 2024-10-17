package model;

import java.io.Serializable;

/**
 * Simple point implementation since JavaFX points are not serializable
 */
public class SerializablePoint2D implements Serializable {
    private double x;
    private double y;

    /**
     * Constructor for a Serializable Point2D
     * @param x x coordinate
     * @param y y coordinate
     */
    public SerializablePoint2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * @return the x coordinate of the point
     */
    public double getX() {
        return x;
    }

    /**
     * @return the y coordinate of the point
     */
    public double getY() {
        return y;
    }
}
