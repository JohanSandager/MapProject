package controller;

import javafx.geometry.Point2D;
import javafx.scene.transform.NonInvertibleTransformException;
import model.Graph;
import view.MapView;

public class PanZoomController {
    private double lastX, lastY; 

    public PanZoomController(MapView view, Graph graph) {
        view.getCanvas().setOnMousePressed(e -> {
            lastX = e.getX(); 
            lastY = e.getY(); 
            //System.out.println("Pressed: " + lastX + ", " + lastY);
            Point2D p;
            try {
                p = view.getGC().getTransform().inverseTransform(lastX, lastY);
                //int i = graph.getIndexFromCoordinates(p.getX(), p.getY());
                System.out.println("pressed@ " + p.getX() + ", " + p.getY()); 
            } catch (NonInvertibleTransformException e1) {
                // not gonna happen 
                e1.printStackTrace();
            }
        });

        view.getCanvas().setOnMouseDragged(e -> {
            if (e.isPrimaryButtonDown()) {
                // gang stuff later
            } else {
                double dx = e.getX() - lastX;
                double dy = e.getY() - lastY;
                view.pan(dx, dy);
            }

            lastX = e.getX();
            lastY = e.getY();
        });

        
        view.getCanvas().setOnScroll(e -> {
            double factor = Math.pow(1.01, e.getDeltaY());
            view.zoom(e.getX(), e.getY(), factor);
        });
    }
}
