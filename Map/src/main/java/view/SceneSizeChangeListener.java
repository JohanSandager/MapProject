package view;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.VBox;

public class SceneSizeChangeListener implements ChangeListener { //Inspired by https://stackoverflow.com/questions/16606162/javafx-fullscreen-resizing-elements-based-upon-screen-size
    Scene scene;
    double initWidth, initHeight;
    Canvas canvas;
    VBox elements;
    MapView mapView;
    /**
     * Listener for canvas to change size when window size is altered
     * @param canvas Where the map is drawn upon
     * @param elements UI element list
     * @param scene The current scene of the application
     * @param initWidth Initial width of the application window
     * @param initHeight Initial height of the application window
     * @param mapView The object that call draw, pan and zoom
     */
    public SceneSizeChangeListener(Scene scene, double initWidth, double initHeight, Canvas canvas, VBox elements, MapView mapView) {
        this.elements = elements;
        this.scene = scene;
        this.initWidth = initWidth;
        this.initHeight = initHeight;
        this.canvas = canvas;
        this.mapView = mapView;
    }
    /**
     * What happens when the listener is activated
     */
    @Override
    public void changed(ObservableValue observable, Object oldValue, Object newValue) {
        double newWidth = scene.getWidth();
        double newHeight = scene.getHeight();

        canvas.setWidth(newWidth - 350);
        canvas.setHeight(newHeight);
        elements.setMaxHeight(canvas.getHeight());
        mapView.draw();
    }
}
