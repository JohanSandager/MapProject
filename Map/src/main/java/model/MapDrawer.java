package model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.transform.NonInvertibleTransformException;
import model.GlobalConfig.Options;
import model.DataTypes.DoubleArrayList;

/**
 * The master class responsible for drawing the map, and any and all elements
 * that should be on the map such as the shortest path or points of interest.
 */
public class MapDrawer {
    private GraphicsContext gc;
    private SerializableRectangle2D bounds;
    private RTree tree;
    private List<SerializablePoint2D> pointsOfInterest;
    private DoubleArrayList graphX, graphY;
    private List<Graph.Edge> graphEdges;
    private Iterable<Graph.Edge> path;
    private ValueChangeSubject<Double> zoomLevel;

    /**
     * The constructor for the MapDrawer
     * 
     * @param gc         the graphic context that should be used to draw
     * @param objects    all loaded objects, the map drawer will handle any sorting
     * @param bounds     the smallest square that contains all loaded objects
     * @param graphX     all x coordinates from the graph
     * @param graphY     all y coordinates from the graph
     * @param graphEdges all edges from the graph
     */
    public MapDrawer(GraphicsContext gc, List<MapObject> objects, SerializableRectangle2D bounds,
            DoubleArrayList graphX, DoubleArrayList graphY, List<Graph.Edge> graphEdges) {
        if (bounds == null || gc == null)
            throw new NullPointerException("Cannot be null");
        this.gc = gc;
        this.bounds = bounds;
        tree = new RTree(bounds, objects);
        this.graphX = graphX;
        this.graphY = graphY;
        this.graphEdges = graphEdges;
        this.path = null;
        this.zoomLevel = new ValueChangeSubject<>();
        pointsOfInterest = new LinkedList<>();
    }

    /**
     * Used to set a point of interest as an address
     */
    public void setPointOfInterest(Address address) {
        setPointOfInterest(address.getGraphNode());
    }

    /**
     * Used to set a point of interest for a specific a node
     * 
     * @param graphNodeID the id of the node to add
     */
    public void setPointOfInterest(int graphNodeID) {
        pointsOfInterest.add(new SerializablePoint2D(graphX.get(graphNodeID), graphY.get(graphNodeID)));
    }

    /**
     * Used to set a point of interest for a specific point
     * 
     * @param point the point to add
     */
    public void setPointOfInterest(SerializablePoint2D point) {
        pointsOfInterest.add(new SerializablePoint2D(point.getX(), point.getY()));
    }

    public void removePointOfInterest() {
        pointsOfInterest.clear();
        ;
    }

    public void setPath(Iterable<Graph.Edge> path) {
        this.path = path;
    }

    private void drawPointOfInterest() {
        double radius = 5 / Math.sqrt(gc.getTransform().determinant()); // nice scales
        for (SerializablePoint2D point : pointsOfInterest) {
            gc.setFill(Color.RED); // set the fill color
            gc.fillOval(point.getX() - radius, point.getY() - radius, 2 * radius, 2 * radius); // draw the circle
        }
    }

    /**
     * The function that is responsible for drawing the entire map, depending on
     * zoom level.
     * It calculates the current view, retrieves the objects from the RTree within
     * that view (and zoom level) and draws them.
     */
    public void draw() {
        gc.setFill(GlobalConfig.getInstance().getBackgroundColor(GlobalConfig.BackgroundColor.CANVAS_BACKGROUND));
        SerializableRectangle2D view = bounds;
        try {
            Point2D upperLeft, lowerRight;

            if (GlobalConfig.getInstance().getOption(Options.DEBUG_ZOOM)) {
                upperLeft = gc.getTransform().inverseTransform(480, 270);
                lowerRight = gc.getTransform().inverseTransform(gc.getCanvas().getWidth() - 480,
                        gc.getCanvas().getHeight() - 270);
            } else {
                upperLeft = gc.getTransform().inverseTransform(0, 0);
                lowerRight = gc.getTransform().inverseTransform(gc.getCanvas().getWidth(), gc.getCanvas().getHeight());
            }

            view = new SerializableRectangle2D(upperLeft.getX(), upperLeft.getY(), lowerRight.getX() - upperLeft.getX(),
                    lowerRight.getY() - upperLeft.getY());

            double differenceOfX = view.getMaxX() - view.getMinX();
            double differenceOfY = view.getMaxY() - view.getMinY();
            double hypotenuse = Math.sqrt(differenceOfX + differenceOfY);
            zoomLevel.setValue(Math.min(1, Math.round(hypotenuse * 100.0) / 100.0));
            gc.setStroke(Color.BLACK);

        } catch (NonInvertibleTransformException e) {
            // never gonna happen (don't jinx)
            e.printStackTrace();
        }

        List<MapObject> toDraw = new ArrayList<>();

        double zoomLvl = zoomLevel.getValue();
        if (GlobalConfig.getInstance().getOption(Options.DRAW_MAP)) {
            tree.getObjectsInSquare(view, toDraw, zoomLvl); // null;//
            if (GlobalConfig.getInstance().getOption(Options.PRINT_DRAW_INFO)) {
                DebugConsole.getInstance().log("@ zoomlvl: " + zoomLvl + " drawing " + toDraw.size() + "objects");
            }
            if (toDraw != null) { // happens if we call it before we do the panning stuff;
                for (MapObject object : toDraw) {
                    if (object != null) {
                        object.draw(gc);
                    }
                }
            }
        }

        if (GlobalConfig.getInstance().getOption(Options.DEBUG_OUTLINE))
            tree.debugOutline(gc, view, zoomLvl);
        if (GlobalConfig.getInstance().getOption(Options.DEBUG_ZOOM)) {
            gc.setStroke(Color.PURPLE);
            gc.setLineWidth(5 / Math.sqrt(gc.getTransform().determinant()));
            gc.strokeRect(view.getMinX(), view.getMinY(), view.getWidth(), view.getHeight());
        }

        drawPointOfInterest();

        if (GlobalConfig.getInstance().getOption(Options.DRAW_GRAPH)) {
            double radius = 2 / Math.sqrt(gc.getTransform().determinant());
            gc.setStroke(Color.GREEN);
            gc.setLineWidth(3 / Math.sqrt(gc.getTransform().determinant()));
            gc.setFill(Color.GREEN);

            Random rnd = new Random();
            for (Graph.Edge edge : graphEdges) {
                gc.setStroke(Color.rgb(rnd.nextInt(255), rnd.nextInt(255), rnd.nextInt(255)));
                gc.strokeLine(graphX.get(edge.fromNode), graphY.get(edge.fromNode), graphX.get(edge.toNode),
                        graphY.get(edge.toNode));
            }
            gc.setFill(Color.BLACK);
            for (int i = 0; i < graphX.size(); i++) {
                gc.fillOval(graphX.get(i) - radius, graphY.get(i) - radius, 2 * radius, 2 * radius);
            }
        }
        if (path == null) {
            return;
        }
        gc.setStroke(Color.RED);
        gc.setLineWidth(5 / Math.sqrt(gc.getTransform().determinant()));
        for (Graph.Edge e : path) {
            gc.strokeLine(graphX.get(e.fromNode), graphY.get(e.fromNode), graphX.get(e.toNode), graphY.get(e.toNode));
        }
    }

    public SerializableRectangle2D getBounds() {
        return bounds;
    }

    public RTree getTree() {
        return tree;
    }

    public ValueChangeSubject<Double> getZoomLevel() {
        return zoomLevel;
    }
}
