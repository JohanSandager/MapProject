package model;

import java.io.Serializable;

/**
 * The MapObjectInfo class presents information that is related to how a
 * MapObject is visualized.
 * This is e.g. zoom level, colors, and stroke width.
 * The class implements Serializable.
 */
public class MapObjectInfo implements Serializable {
    private double zoomLevel;
    private SerializableColor[] colors;
    private double strokeWidth;

    /**
     * The Constructor for MapObjectInfo constructs a MapObjectInfo object with the
     * given zoom level, colors, and stroke width.
     * 
     * @param zoomLevel   the zoom level at which the MapObject should be displayed
     * @param colors      an array of SerializableColors that stores the stroke
     *                    colors for the MapObject
     * @param strokeWidth the width of the stroke used to draw the MapObject
     */
    public MapObjectInfo(double zoomLevel, SerializableColor[] colors, double strokeWidth) {
        this.zoomLevel = zoomLevel;
        this.colors = colors;
        this.strokeWidth = strokeWidth;
    }

    /**
     * The getZoomLevel method returns the current zoomlevel
     * 
     * @return current zoomLevel
     */
    public double getZoomLevel() {
        return zoomLevel;
    }

    /**
     * Gets fill color for the MapObject.
     * 
     * @return the fill color as a SerializableColor object
     */
    public SerializableColor getColor() {
        if (GlobalConfig.getInstance().getOption(GlobalConfig.Options.TOGGLE_DARKMODE)) {
            return colors[1];
        } else {
            return colors[0];
        }
    }

    /**
     * Gets stroke width for the MapObject.
     * 
     * @return the stroke width.
     */
    public double getStrokeWidth() {
        return strokeWidth;
    }
}
