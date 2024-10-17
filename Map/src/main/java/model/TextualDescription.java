package model;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

/**
 * Responsible for generating a textual route description based on the given path.
 */
public class TextualDescription {

    /**
     * Generates the textual description
     * @param graph the graph on which the Dijkstra was performed
     * @param path the path to make the textual description for
     * @return a list of string instructions
     */
    public static Iterable<String> getTextualDescription(Graph graph, Iterable<Graph.Edge> path) {
        List<String> textualDescription = new ArrayList<>();
        double roadTotalDistance = 0.0;
        Highway lastHighway = null;
        Point2D lastFromPoint = null;

        for(Graph.Edge edge : path) {
            double fromX = graph.getXFromID(edge.fromNode);
            double fromY = graph.getYFromID(edge.fromNode);
            double toX = graph.getXFromID(edge.toNode);
            double toY = graph.getYFromID(edge.toNode);
            Point2D currentFrom = new Point2D(fromX, fromY);
            Point2D toPoint = new Point2D(toX, toY);

            if(lastHighway == edge.highway) {
                roadTotalDistance += edge.distance;
            } else if (lastHighway == null) {
                lastFromPoint = currentFrom;
                lastHighway = edge.highway;
                roadTotalDistance += edge.distance;
            } else {
                String instruction = rightLeftOrStraight(lastFromPoint, currentFrom, toPoint);
                textualDescription.add(instruction + ":" + "Go " + instruction + " on " + edge.highway.name + ":" + Math.round(roadTotalDistance * 100.0) / 100.0);
                roadTotalDistance = 0.0;
                roadTotalDistance += edge.distance;
                lastHighway = edge.highway;
                lastFromPoint = currentFrom;
            }
        }
        return textualDescription;
    }

    /**
     * This function will determine which direction the traveler should go in relation to the heading.
     * It does this by creating two vectors, one for the previous traveled edge and one for the next edge.
     * These share one common node, which will act as the basis for entertaining the angle.
     * <p>
     * The angle is determined by finding the cross-product
     * @param lastFromPoint the point starting the previous edge
     * @param currentFromPoint the shared point of the two edges
     * @param toPoint the ending point of the next edge
     * @return what direction to turn
     */
    private static String rightLeftOrStraight(Point2D lastFromPoint, Point2D currentFromPoint, Point2D toPoint) {
        double xLengthOfVector1 = lastFromPoint.getX() - currentFromPoint.getX();
        double yLengthOfVector1 = lastFromPoint.getY() - currentFromPoint.getY();
        double xLengthOfVector2 = toPoint.getX() - currentFromPoint.getX();
        double yLengthOfVector2 = toPoint.getY() - currentFromPoint.getY();
        Point2D vector1 = new Point2D(xLengthOfVector1, yLengthOfVector1).normalize();
        Point2D vector2 = new Point2D(xLengthOfVector2, yLengthOfVector2).normalize();
        double angle = vector1.getX() * vector2.getY() - vector2.getX() * vector1.getY();
        if(Math.abs(angle) > 0.3) return angle < 0 ? "right" : "left";
        return "straight";
    }
}