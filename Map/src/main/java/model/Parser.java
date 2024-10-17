package model;

import javafx.scene.canvas.GraphicsContext;

import java.util.List;

/**
 The Parser interface has methods to parse a list of strings representing a map and create
 corresponding objects to draw it on a canvas using a GraphicsContext object.
 */
public interface Parser {

    /**
     * Parses a list of strings representing a map and generates a MapDrawer object to draw it on a
     * canvas using a GraphicsContext object.
     * @param lines the list of strings representing the map
     * @param gc the GraphicsContext object to use for drawing the map
     * @return a MapDrawer object to draw the map
     */
    public MapDrawer parse(List<String> lines, GraphicsContext gc);
    /**
     * Parses a single string representing a line and generates a corresponding MapObject to draw
     * it on a canvas using a GraphicsContext object.
     * @param line the string representing the line
     * @param gc the GraphicsContext object to use for drawing the object
     * @return a MapObject to draw the object
     */
    public MapObject parseLine(String line, GraphicsContext gc);
}
