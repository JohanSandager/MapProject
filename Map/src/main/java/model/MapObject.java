package model;

import javafx.scene.canvas.GraphicsContext;

import java.io.Serializable;
import java.util.List;

/**
 * Abstract class representing a map object, containing its coordinates, bounds,
 * size, and zoom level.
 */
public abstract class MapObject implements Serializable {
    protected double[] coordinates;
    protected SerializableRectangle2D culBounds;
    protected double sz;
    protected static final double zoomLevel = 1; // draw if zoomlevel is less than this, zoomlevel can at most be

    /**
     * Constructor for MapObject class.
     *
     * @param coordinates A list of nodes representing the object's coordinates
     */
    public MapObject(List<Node> coordinates) {
        this.coordinates = new double[coordinates.size() * 2];
        Node origNode = coordinates.get(0);
        double minX, minY;
        double maxX = minX = 0.56 * origNode.lon;
        double maxY = minY = -origNode.lat;
        for (int i = 0; i < coordinates.size(); ++i) {
            var node = coordinates.get(i);
            double x, y;
            this.coordinates[2 * i + 1] = y = -node.lat;
            this.coordinates[2 * i] = x = 0.56 * node.lon;

            if (minX > x) {
                minX = x;
            }
            if (minY > y) {
                minY = y;
            }
            if (maxX < x) {
                maxX = x;
            }
            if (maxY < y) {
                maxY = y;
            }

        }
        this.culBounds = new SerializableRectangle2D(minX, minY, maxX - minX, maxY - minY);
        sz = Math.sqrt(Math.pow(culBounds.getWidth(), 2) + Math.pow(culBounds.getHeight(), 2));
    }

    /**
     * Abstract method for drawing the map object on the graphics context.
     *
     * @param gc The graphics context to draw on
     */
    public abstract void draw(GraphicsContext gc);

    /**
     * Abstract method for getting the zoom level of the map object.
     *
     * @return The zoom level of the map object
     */
    public abstract double getZoomLevel(); // we have to make this abstract because java doesnt allow overriding of
                                           // fields for some reason

    /**
     * Get the bounds of the map object.
     *
     * @return The bounds of the map object
     */
    public SerializableRectangle2D getBounds() {
        return culBounds;
    }

    /**
     * Get the size of the map object.
     *
     * @return The size of the map object
     */
    public double getSize() {
        return sz;
    }
}
