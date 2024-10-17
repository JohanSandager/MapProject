package model;

import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * The Road class extends a MapObject and represents a road consisting of
 * multiple nodes.
 * It overrides MapObject's draw and getZoomLevel methods.
 */
public class Road extends MapObject {
    protected static final double zoomLevel = 0.2;

    /**
     * Creates a Road object with the given list of nodes.
     * 
     * @param nodes the list of nodes that make up the road
     */
    public Road(List<Node> nodes) {
        super(nodes);
    }

    /**
     * Draws the road on the given graphics context gc.
     * If the road has less than 2 coordinates, the function is returned and nothing
     * is drawn.
     * 
     * @param gc the graphics context on which to draw the road
     */
    @Override
    public void draw(GraphicsContext gc) {
        if (coordinates.length < 2) {
            return;
        }
        gc.beginPath();
        gc.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length; i += 2) {
            gc.lineTo(coordinates[i], coordinates[i + 1]);
        }
        gc.stroke();
    }

    /**
     * Returns the zoom level for rendering the road.
     * 
     * @return zoom level
     */
    @Override
    public double getZoomLevel() {
        return zoomLevel;
    }
}
