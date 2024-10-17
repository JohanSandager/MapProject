package model;

import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * The `Highway` class represents a specific type of road that is typically a
 * major route for transportation.
 * It extends the `Road` class and adds the ability to store a `MapObjectInfo`
 * object and a name.
 */
public class Highway extends Road {
    /**
     * The `MapObjectInfo` object that stores information about the highway's color,
     * width, and zoom level.
     */
    protected MapObjectInfo info;
    /**
     * The name of the highway.
     */
    protected String name;

    /**
     * Constructs a `Highway` object with the specified list of nodes,
     * `MapObjectInfo` object, and name.
     *
     * @param nodes The list of `Node` objects that define the highway.
     * @param info  The `MapObjectInfo` object that stores information about the
     *              highway's color, width, and zoom level.
     * @param name  The name of the highway.
     */
    public Highway(List<Node> nodes, MapObjectInfo info, String name) {
        super(nodes);
        this.info = info;
        this.name = name;
    }

    /**
     * Gets the name of the highway.
     *
     * @return The name of the highway.
     */
    public String getName() {
        return name;
    }

    /**
     * Draws the highway on the specified `GraphicsContext`.
     *
     * @param gc The `GraphicsContext` on which to draw the highway.
     */
    @Override
    public void draw(GraphicsContext gc) {
        gc.setLineWidth(gc.getLineWidth() * info.getStrokeWidth());
        gc.setStroke(info.getColor().getFXColor());
        super.draw(gc);
        gc.setLineWidth(gc.getLineWidth() / info.getStrokeWidth());
    }

    /**
     * Gets the zoom level of the highway.
     *
     * @return The zoom level of the highway.
     */
    @Override
    public double getZoomLevel() {
        return info.getZoomLevel();
    }
}
