package model;

import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 * This class is used to represent a MapObject that covers an area, rather than
 * a collections of lines
 */
public class Area extends MapObject {
    protected static final double zoomLevel = 0.1;

    double[] xCoordinates;
    double[] yCoordinates;
    int xCounter = 0;
    int yCounter = 0;
    private MapObjectInfo info;

    public Area(List<Node> nodes, MapObjectInfo info) {
        super(nodes);
        this.info = info;
        xCoordinates = new double[nodes.size()];
        yCoordinates = new double[nodes.size()];
        for (int i = 0; i < coordinates.length; i++) {
            if (i % 2 == 0) {
                xCoordinates[xCounter] = coordinates[i];
                xCounter++;
            } else {
                yCoordinates[yCounter] = coordinates[i];
                yCounter++;
            }
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setFill(info.getColor().getFXColor());
        gc.fillPolygon(xCoordinates, yCoordinates, coordinates.length / 2);
        gc.stroke();
    }

    @Override
    public double getZoomLevel() {
        return info.getZoomLevel();
    }
}
