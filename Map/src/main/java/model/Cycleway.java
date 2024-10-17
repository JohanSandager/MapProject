package model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * The cycleway class represents roads that are only cyclable.
 * It extends the Road-class and overrides the draw method, so it can be drawn
 * in a different color than the usual roads.
 * It also defines a zoom-level that are specific to footways.
 */
public class Cycleway extends Road {
    /**
     * The zoom level of the cycleway
     */
    protected static final double zoomLevel = 0.2;

    /**
     * Constructs a Cycleway with the given nodes.
     *
     * @param nodes A List of nodes that make up the Cycleway.
     */
    public Cycleway(List<Node> nodes) {
        super(nodes);
    }

    /**
     * Draws the Cycleway on the GraphicsContext using the specified stroke color.
     *
     * @param gc The GraphicsContext to draw on.
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setStroke(Color.rgb(75, 89, 110, 1.0));
        super.draw(gc);
    }

    /**
     * Gets the zoom level for the Cycleway.
     *
     * @return The zoom level for the Cycleway.
     */
    @Override
    public double getZoomLevel() {
        return zoomLevel;
    }
}
