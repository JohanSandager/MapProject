package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

/**
 * The class Wayline extends MapObject and implements Serializable.
 * It maintains a zoomLevel, contains a draw method to draw the way as a line
 * and a get zoomlevel method.
 */
public class WayLine extends MapObject {
    protected static final double zoomLevel = 1.5;

    /**
     * Constructor for Wayline
     * 
     * @param List of nodes that make up the way
     */
    public WayLine(List<Node> nodes) {
        super(nodes);
    }

    /**
     * Draw method for drawing the wayline
     * 
     * @param gc the graphic contect to draw on
     */
    @Override
    public void draw(GraphicsContext gc) {
        if (coordinates.length < 2) {
            return;
        }
        gc.beginPath();
        gc.moveTo(coordinates[0], coordinates[1]);
        // System.out.println("Drawing @ (" + coordinates[0] + ", " + coordinates[1] +
        // ")");
        for (int i = 2; i < coordinates.length; i += 2) {
            gc.lineTo(coordinates[i], coordinates[i + 1]);
        }
        gc.setStroke(Color.BLACK);
        gc.stroke();
    }

    /**
     * The getZoomLevel()-method is getting ZoomLevel
     * 
     * @return double
     */
    @Override
    public double getZoomLevel() {
        return zoomLevel;
    }
}
