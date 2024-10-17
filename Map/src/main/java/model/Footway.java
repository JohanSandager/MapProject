package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * The footway class represents roads that are only walkable.
 * It extends the Road-class and overrides the draw method, so it can be drawn
 * in a different color than the usual roads.
 * It also defines a zoom-level that are specific to footways.
 */
public class Footway extends Road {
    /**
     * The zoom-level for footways
     */
    protected static final double zoomLevel = 0.1;

    /**
     * Creates a new Footway object with the given list of nodes.
     *
     * @param nodes the list of nodes that make up the footway
     */
    public Footway(List<Node> nodes) {
        super(nodes);
    }

    /**
     * Overrides the draw method to set the stroke color to a specific value for
     * footways.
     *
     * @param gc the graphics context on which to draw the footway
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.rgb(75, 89, 110, 1.0));
        super.draw(gc);
    }

    /**
     * Returns the zoom level for footways.
     *
     * @return the zoom level for footways
     */
    @Override
    public double getZoomLevel() {
        return zoomLevel;
    }
}
