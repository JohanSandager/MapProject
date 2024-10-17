package model;

import java.util.Comparator;

/**
 * This class is a custom comparator for comparing MapObjects on their bounds.
 * The comparison can be done both along the X-axis or the Y-axis depending if
 * we xWay parameter is passed to the constructor.
 * We use this comparator in R-trees for sorting the elements in each node.
 */
public class MapObjectComparator implements Comparator<MapObject> {
    private boolean xWay;

    /**
     * The constructor for MapObjectComparator.
     * 
     * @param xWay if true, the comparison will be along the X-axis, otherwise it
     *             will be along the Y-axis.
     */
    public MapObjectComparator(boolean xWay) {
        this.xWay = xWay;
    }

    /**
     * Compares two MapObjects based on their bounds.
     * 
     * @param o1 the first MapObject
     * @param o2 the second MapObject
     * @return a negative integer (when less than), zero (when equal to), or a
     *         positive integer (when greater than)
     */
    @Override
    public int compare(MapObject o1, MapObject o2) {
        SerializableRectangle2D r1 = o1.getBounds();
        SerializableRectangle2D r2 = o2.getBounds();

        if (xWay) {
            if (r1.getMaxX() < r2.getMaxX()) {
                // if(r1.getMinX() < r2.getMinX()) { return -1; }
                return -1;
            } // &&
            if (r1.getMaxX() > r2.getMaxX()) {
                // if(r1.getMinX() > r2.getMinX()) { return 1; }
                return 1;
            } // &&
            return 0;
        } else {
            if (r1.getMaxY() < r2.getMaxY()) {
                // if(r1.getMinY() < r2.getMinY()) { return -1; }
                return -1;
            } // &&
            if (r1.getMaxY() > r2.getMaxY()) {
                // if(r1.getMinY() > r2.getMinY()) { return 1; }
                return 1;
            } // &&
            return 0;
        }
    }
}