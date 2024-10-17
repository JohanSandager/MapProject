package model;

import java.io.Serializable;

/**
 * A simple implementation of the Rectangle2D from JavaFX, since it is not Serializable
 */
public class SerializableRectangle2D implements Serializable {
    double minX;
    double minY;
    double width;
    double height;

    /**
     * Constructor for a Serializable Rectangle 2D using coordinates, width and height
     * @param minX the minimum x coordinate of the rectangle
     * @param minY the minimum y coordinate of the rectangle
     * @param width the width
     * @param height the height
     */
    public SerializableRectangle2D(double minX, double minY, double width, double height) {
        this.minX = minX;
        this.minY = minY;
        this.width = width;
        this.height = height;
    }

    public double getMinX() {
        return minX;
    }

    public double getMinY() {
        return minY;
    }

    public double getMaxX() {
        return minX + width;
    }

    public double getMaxY() {
        return minY + height;
    }

    public double getHeight() {
        return height;
    }

    public double getWidth() {
        return width;
    }

    /**
     * Checks if a point is within the limits of the rectangle
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return true if contained, false if not
     */
    public boolean contains(double x, double y) {
        SerializablePoint2D point = new SerializablePoint2D(x, y);
        return contains(point);
    }

    /**
     * Checks if a point is within the limits of the rectangle
     * @param point the point to check for
     * @return true if contained, false if not
     */
    public boolean contains(SerializablePoint2D point) {
        double pointX = point.getX();
        double pointY = point.getY();
        return pointX >= minX && pointX <= getMaxX() && pointY >= minY && pointY <= getMaxY();
    }

    /**
     * Checks if a rectangle is within the limits of the rectangle
     * @param x minimum x coordinate of the rectangle
     * @param y minimum y coordinate of the rectangle
     * @param w width of the rectangle
     * @param h height of the rectangle
     * @return true if fully contained, false if not
     */
    public boolean contains(double x, double y, double w, double h) {
        SerializableRectangle2D rectangle = new SerializableRectangle2D(x, y, w, h);
        return contains(rectangle);
    }

    /**
     * Checks if a rectangle is within the limits of the rectangle
     * @param rectangle the rectangle to check weather contained
     * @return true if fully contained, fasle if not
     */
    public boolean contains(SerializableRectangle2D rectangle) {
        SerializablePoint2D bottomLeftCorner = new SerializablePoint2D(rectangle.getMinX(), rectangle.getMinY());
        SerializablePoint2D topRightCorner = new SerializablePoint2D(rectangle.getMaxX(), rectangle.getMaxY());
        return contains(bottomLeftCorner) && contains(topRightCorner);
    }

    /**
     * Checks if a rectangle intersects the rectangle
     * @param x minimum x coordinate of the rectangle
     * @param y minimum y coordinate of the rectangle
     * @param w width of the rectangle
     * @param h height of the rectangle
     * @return true if any part of given rectangle intersects the rectangle, false if not
     */
    public boolean intersects(double x, double y, double w, double h) {
        SerializableRectangle2D rectangle = new SerializableRectangle2D(x, y, w, h);
        return intersects(rectangle);
    }

    /**
     * Checks if a rectangle intersects the rectangle
     * @param rectangle the rectangle to check whether intersects
     * @return true if any part of given rectangle intersects the rectangle, false if not
     */
    public boolean intersects(SerializableRectangle2D rectangle) {
        return !(rectangle.getMinX() >= minX + width || minX >= rectangle.getMaxX() || rectangle.getMinY() >= minY + height || minY >= rectangle.getMaxY());
    }
}
