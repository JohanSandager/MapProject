package view;

import java.util.Random;

import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.stage.Stage;
import javafx.scene.input.MouseButton;
import model.*;

public class MapView {
    private Canvas canvas;
    private GraphicsContext gc;
    private Affine trans;
    private boolean poiMode = false;
    MapDrawer drawer;

    /**
     * This class controls the map-portion of the GUI. It ´controls everything from
     * drawing to pan/zoom
     * 
     * @param primaryStage The main window
     * @param scene        The current scene of the program
     * @param canvas       Where the map is drawn
     * @param gc           A GraphicsContext that handles the drawing
     * @param drawer       a MapDrawer that decides what should be drawn
     */
    public MapView(Stage primaryStage, Scene scene, Canvas canvas, GraphicsContext gc, MapDrawer drawer) {
        this.canvas = canvas;
        this.gc = gc;
        this.drawer = drawer;
        trans = new Affine();

        primaryStage.setScene(scene);
        primaryStage.show();

        System.out.println("minX: " + drawer.getBounds().getMinX() + ", maxX:" + drawer.getBounds().getMaxX() +
                ", minY: " + drawer.getBounds().getMinY() + ", maxY:" + drawer.getBounds().getMaxY());

        pan(-drawer.getBounds().getMinX() + drawer.getBounds().getWidth() / 2,
                -drawer.getBounds().getMinY() + drawer.getBounds().getHeight());
        zoom(0, 0, canvas.getHeight() / (drawer.getBounds().getMaxX() - drawer.getBounds().getMinX()));
        draw();
        canvas.setOnMouseClicked(event -> handleMouseClick(event));
    }

    /**
     * Getter for canvas
     */
    public Canvas getCanvas() {
        return canvas;
    }

    /**
     * Method for toggling "Point of Interest"-mode
     * 
     * @param mode Toggles on if true
     */
    public void setPOIMode(boolean mode) {
        this.poiMode = mode;
    }

    /**
     * Getting the current status of "Point of Intereset"-mode. Enabled if true
     */
    public boolean getPOIMode() {
        return this.poiMode;
    }

    /**
     * Handles mouseclick for point of interests
     * 
     * @param event The current mouse event e.g. left-click
     */
    private void handleMouseClick(MouseEvent event) {
        try {
            if (poiMode && event.getButton() == MouseButton.PRIMARY) {
                Point2D point = trans.inverseTransform(event.getX(), event.getY()); // convert screen coordinates to map
                                                                                    // coordinates
                SerializablePoint2D newPoint = new SerializablePoint2D(point.getX(), point.getY());
                drawer.setPointOfInterest(newPoint);
                draw();
            }
        } catch (NonInvertibleTransformException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getting the current GraphicsContext
     */
    public GraphicsContext getGC() {
        return gc;
    }

    /**
     * Initiates a draw-call to the map drawer.
     * Cleans out the canvas before drawing
     */
    public void draw() {
        gc.setTransform(new Affine());
        Random rnd = new Random();
        Color color = GlobalConfig.getInstance().getBackgroundColor(GlobalConfig.BackgroundColor.CANVAS_BACKGROUND);
        gc.setFill(color);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setLineWidth(1 / Math.sqrt(trans.determinant()));
        gc.setStroke(Color.BLACK);
        drawer.draw();

        FPSCounter.getInstance().updateFPS(System.currentTimeMillis());
    }

    /**
     * Handles panning on the map
     * 
     * @param x Cursor x-coord when panning
     * @param y Cursor y-coord when pannign
     */
    public void pan(double x, double y) {
        trans.prependTranslation(x, y);
        draw();
    }

    /**
     * Handles zooming on the map
     * 
     * @param x      Cursor x-coord when zooming
     * @param y      Cursor y-coord when zooming
     * @param factor The predetermined zoom factor
     */
    public void zoom(double x, double y, double factor) {
        pan(-x, -y);
        trans.prependScale(factor, factor);
        pan(x, y);
    }
}
