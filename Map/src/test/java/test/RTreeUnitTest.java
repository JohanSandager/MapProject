package test;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import javafx.geometry.Rectangle2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RTreeUnitTest {

    private FileLoader loader;
    private List<MapObject> objs;

    @BeforeEach void setup() {
        try {
            loader = new FileLoader("./src/test/test-data/thuro.osm");
            objs = loader.getObjects();
        } catch (XMLStreamException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test void constructorThrowsNullWhenPassedNullObjects() {
        assertThrows(NullPointerException.class, () -> {
            RTree tree = new RTree(loader.getBounds(), null);
        });
    }

    @Test void isInBoundsReturnTrueOnObjectIntersects() {
        RTree tree = new RTree(loader.getBounds(), objs);
        SerializableRectangle2D shouldBeInBound = new SerializableRectangle2D(5, -55, 1, 1);
        assertTrue(tree.inBounds(shouldBeInBound));
    }

    @Test void isInBoundsReturnFalseOnObjectNotInBounds() {
        RTree tree = new RTree(loader.getBounds(), objs);
        SerializableRectangle2D shouldNotBeInBound = new SerializableRectangle2D(420, 69, 666, 1337);
        assertFalse(tree.inBounds(shouldNotBeInBound));
    }

    @Test void getBestDistanceReturnsTheExpectedDistanceWhenXIsInBoundsAndYIsNotInBounds() {
        RTree tree = new RTree(loader.getBounds(), objs);
        SerializablePoint2D point = new SerializablePoint2D(6, -420);
        double expectedDistance = Math.abs(tree.getBounds().getMinY() + 420);
        assertEquals(tree.getBestDistance(point), expectedDistance);
    }

    @Test void getBestDistanceReturnsTheExpectedDistanceWhenYIsInBoundsAndXIsNotInBounds() {
        RTree tree = new RTree(loader.getBounds(), objs);
        SerializablePoint2D point = new SerializablePoint2D(420, -55.02);
        double expectedDistance = Math.abs(tree.getBounds().getMaxX() - 420);
        assertEquals(tree.getBestDistance(point), expectedDistance);
    }

    @Test void getBestDistanceReturnsTheExpectedDistanceWhenNeitherXAndYAreInBounds() {
        RTree tree = new RTree(loader.getBounds(), objs);
        SerializablePoint2D point = new SerializablePoint2D(tree.getBounds().getMaxX() + 2, tree.getBounds().getMaxY() + 2);
        double expectedDistance = Math.sqrt(4 + 4); //2^2
        assertEquals(tree.getBestDistance(point), expectedDistance, 0.00001);
    }

    @Test void getBestDistanceReturnsZeroOnInputInsideBounds() {
        RTree tree = new RTree(loader.getBounds(), objs);
        SerializablePoint2D point = new SerializablePoint2D(6, -55.02);
        assertEquals(tree.getBestDistance(point), 0.0);
    }

    @Test void returnsCorrectAmountOfObjectsInSquareCoveringAll() {
        RTree tree = new RTree(loader.getBounds(), objs);
        SerializableRectangle2D rectangleThatContainsAll = new SerializableRectangle2D(-100, -100, 200, 200);
        List<MapObject> returnedObjects = new ArrayList<>();
        tree.getObjectsInSquare(rectangleThatContainsAll, returnedObjects, 0.01);
        assertEquals(returnedObjects.size(), 5014);
    }

   @Test void returnsCorrectAmountOfObjectsInSquareCoveringSmallerKnownArea() {
       RTree tree = new RTree(loader.getBounds(), objs);
       SerializableRectangle2D rectangleThatContainsAll = new SerializableRectangle2D(5.973946391595829, -55.035363496188204, (5.984454466578112 - 5.973946391595829), (-55.029452704010666 + 55.035363496188204));
       List<MapObject> returnedObjects = new ArrayList<>();
       tree.getObjectsInSquare(rectangleThatContainsAll, returnedObjects, 0.14);
       assertEquals(returnedObjects.size(), 611);
   }

   @Test void getsCorrectNearestHighway() {
       RTree tree = new RTree(loader.getBounds(), objs);
       String nearestHighway= tree.getNearestRoad(5.986324488064858, -55.049577512171545).getName();
       assertEquals(nearestHighway, "Nordvej");
   }
}
