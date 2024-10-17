package test;

import controller.PanZoomController;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import model.FileLoader;
import model.MapDrawer;
import model.MapObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MapDrawerUnitTest {

    private Canvas canvas;
    private GraphicsContext gc;
    private MapDrawer drawer;
    private FileLoader loader;
    private List<MapObject> objs;

    @BeforeEach void setup() {
        canvas = new Canvas(1920, 1080);
        gc = canvas.getGraphicsContext2D();
        try {
            loader = new FileLoader("./src/test/test-data/thuro.osm");
            objs = loader.getObjects();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test void getBoundsReturnsCorrectValue() {
        drawer = new MapDrawer(gc, objs, loader.getBounds(), null,null,null);
        assertEquals(drawer.getBounds().getMinX(), 5.961928, 0.0001);
        assertEquals(drawer.getBounds().getMinY(), -55.0291, 0.0001);
        assertEquals(drawer.getBounds().getMaxX(), 6.002528, 0.0001);
        assertEquals(drawer.getBounds().getMaxY(), -55.002, 0.0001);
    }

    @Test void constructorThrowsNullPointerExceptionWithNullGc() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            drawer = new MapDrawer(null, objs, loader.getBounds(), null,null,null);
        });
    }

    @Test void constructorThrowsNullPointerExceptionWithNullObjects() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            drawer = new MapDrawer(gc, null, loader.getBounds(), null,null,null);
        });
    }

    @Test void constructorThrowsNullPointerExceptionWithNullBounds() {
        Exception exception = assertThrows(NullPointerException.class, () -> {
            drawer = new MapDrawer(gc, objs, null, null,null,null);
        });
    }
}
