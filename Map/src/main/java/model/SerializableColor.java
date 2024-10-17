package model;

//Borrowed from StackOverflow: https://stackoverflow.com/questions/36748358/saving-color-as-state-in-a-javafx-application

import java.io.Serializable;
import javafx.scene.paint.Color;

/**
 * A wrapper class for the javafx color, since it is not serializable
 */
public class SerializableColor implements Serializable
{
    private double red;
    private double green;
    private double blue;
    private double alpha;


    /**
     * The constructor for a new Serializable color using a javafx color
     * @param color the javafx color to save
     */
    public SerializableColor(Color color)
    {
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        this.alpha = color.getOpacity();
    }

    /**
     * The constructor for a new Serializable color using RGB
     * @param red red value
     * @param green green value
     * @param blue blue value
     */
    public SerializableColor(double red, double green, double blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = 1;
    }

    /**
     * @return the color as a JavaFX color object
     */
    public Color getFXColor()
    {
        return new Color(red, green, blue, alpha);
    }
}
