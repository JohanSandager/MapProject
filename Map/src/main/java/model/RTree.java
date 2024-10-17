package model;

import java.io.Serializable;
import java.util.*;


import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

/**
 * RTree is a spatial data structure for getting objects in multi-dimensional space in an effective way.
 * The class creates an RTree with a given list of MapObjects and a square bounds.
 * It keeps track of parent and children, different mapobjects, zoomlevels, distance to its center and objects shown pr square.
 * The class supports insertion of MapObjects, querying for MapObjects within a given bounds, and splitting objects
 * with identical zoom levels into clumps at the same zoom level to make sure for better querying.
 */
public class RTree implements Serializable{
    public SerializableRectangle2D bounds;
    public double distToCenter; 
    private RTree parent; 
    private RTree[] children;
    private List<MapObject> data; 
    private double zoomLevel;
    public final static int objPerSquare = 25;
    public final static double zoomDiffPerLevel = 0.2;


    /**
     Initializes an RTree object with the given bounds and list of MapObjects. The constructor
     sorts the objects in the list by their x-coordinate. It also creates columns of objects and sorts
     those columns by their y-coordinate. Each column is subdivided into leaves that is made sure to at most contain
     objPerSquare MapObjects, which is defined to 25 initially in the class field.
     The bounds of the root node is set to the max and min bounds of all the MapObjects.
     The structore of the RTree is constructed bottom-up. It stats from the leaves and goes up towards the root.
     It is constructed by combining columns pairwise.
     * @param bounds the surrounding bounds, within whom all objects exist 
     * @param objects the list of all objects to include in the tree 
     */
    public RTree(SerializableRectangle2D bounds, List<MapObject> objects) {
        objects.sort(new MapObjectComparator(true));

        distToCenter = 0; // this is the root node 
        parent = null; // this is the root node

        int iterations = Double.valueOf(Math.sqrt(objects.size() / objPerSquare)).intValue();
        if((Math.pow(iterations, 2) * objPerSquare) < objects.size()) { iterations++; }

        int objPerColumn = objPerSquare * iterations;

        double minX = objects.get(0).getBounds().getMinX(); 
        double maxX = objects.get(objects.size() - 1).getBounds().getMaxX(); 
        double absMinX = minX; 
        double absMaxY = -999, absMinY = 9999999;
         
        int x = 0, y = 0, amtObjects = 0; 

        // Here we loop over the entire objects list, sorted along maxX. 
        // We subdivide this list to objects.size() / objPerColumn sublists, which represents our columns
        // We then sort these columns by maxY
        int amtXIt = Math.min(objects.size() / objPerColumn + 1, iterations);
        RTree[][] leafs = new RTree[amtXIt][];
        for(int i = 0; i < amtXIt; i += 1) {
            x = i * objPerColumn; 
            int xEnd = Math.min(objects.size(), x + objPerColumn);
            List<MapObject> subList = objects.subList(x, xEnd);

            minX = maxX = subList.get(subList.size() - 1).getBounds().getMaxX(); 

            subList.sort(new MapObjectComparator(false));

            double minY = subList.get(0).getBounds().getMinY();
            
            // Loop over the column we just created and subdivide it into leafs. The column is sorted by maxY
            // These leafs have the size of <=objPerSquare.  
            int amtYIt = subList.size() / objPerSquare + (subList.size() % objPerSquare == 0 ? 0 : 1);
            amtYIt = Math.min(iterations, subList.size() / objPerSquare + 1);
            y = 0;
            List<RTree> newColumn = new ArrayList<>(); 
            for(int k = 0; k < amtYIt; k += 1) {
                y = k * objPerSquare; 
                int yEnd = Math.min(subList.size(), y + objPerSquare);
                List<MapObject> subSubList = subList.subList(y, yEnd); // the list representing this column

                // we know exactly what maxY is since the sublist is sorted by it 
                double maxY = subSubList.get(subSubList.size() - 1).getBounds().getMaxY();

                // we cant really say anything about what minX, minY and maxX are in this particular subSubList, so we need to run through and find them
                minY = subSubList.get(0).getBounds().getMinY();
                minX = subSubList.get(0).getBounds().getMinX();
                maxX = subSubList.get(0).getBounds().getMaxX();
                for(int j = 1; j < subSubList.size(); j++) {
                    if(subSubList.get(j).getBounds().getMinY() < minY) { minY = subSubList.get(j).getBounds().getMinY(); }
                    if(subSubList.get(j).getBounds().getMinX() < minX) { minX = subSubList.get(j).getBounds().getMinX(); }
                    if(subSubList.get(j).getBounds().getMaxX() > maxX) { maxX = subSubList.get(j).getBounds().getMaxX(); }
                }

                // keep track of the absolute coordinates, used for the bounds of the root node 
                if(minY < absMinY) { absMinY = minY; } 
                if(maxY > absMaxY) { absMaxY = maxY; }
                if(minX < absMinX) { absMinX = minX; }

                // create the new leaf, with the particular bounds we know are true for this set of objects, and the objects 
                newColumn.add(new RTree(minX, maxX, minY, maxY, subSubList));
            }
            leafs[i] = newColumn.toArray(new RTree[0]); // we have completed this column, and add it to the top level array
        }
        // we set the bounds of the root node to the maximum/minimum bounds for all the objects. 
        this.bounds = new SerializableRectangle2D(absMinX, absMinY, maxX - absMinX, Math.abs(absMaxY - absMinY));

        RTree[][] activeLayer = leafs; // activeLayer is the layer/level of the tree that we are currently going over 
        double curLayerZoomLevel = 0.4; 
        while(activeLayer.length > 1) {
            List<RTree[]> newLayer = new ArrayList<>(); // the next "layer"/"level" of the tree that we are going to construct

            int i = 1; // since we go in increments of 2, remember 'i' so we can fix any off-by-one error at the end
            for(; i < activeLayer.length; i += 2) { // we loop over the active layer in pairs of 2 
                RTree[] minColumn; // column with the lowest amount of rows, equivalently the one with the tallest rows
                RTree[] otherColumn; // and the other columns
                
                // set the arrays to the appropiate columns 
                if(activeLayer[i].length < activeLayer[i-1].length) { 
                    minColumn = activeLayer[i]; 
                    otherColumn = activeLayer[i-1]; 
                } else {
                    minColumn = activeLayer[i-1]; 
                    otherColumn = activeLayer[i]; 
                }

                int j = 0; // how far column "B" we are, need to remember this independently of the minColumn loop 
                List<RTree> newCol = new ArrayList<>();  // the new column of combined rows

                // here we loop over the minColumn and combine the rows by combining each row of the minColumn with all the rows from j to the one with the smallest 
                // difference in maxY coordinate This works since we know both columns are sorted by maxY.  
                for (RTree rTree : minColumn) {
                    // we initialize minDiff to the maximum difference between any two rows,
                    double minDiff = Math.max(Math.abs(otherColumn[otherColumn.length - 1].bounds.getMaxY() - minColumn[0].bounds.getMinY())
                            , Math.abs(minColumn[minColumn.length - 1].bounds.getMaxY() - otherColumn[0].bounds.getMinY()));


                    List<MapObject> escalatedObjects = new ArrayList<>(); // the objects that have a higher zoom level, that we add to the new layer we create

                    List<RTree> newLeaf = new ArrayList<>(); // the new column we will construct
                    handleLeaf(rTree, newLeaf, escalatedObjects, curLayerZoomLevel);
                    // grab some preliminary bounds, if any of them are inaccurate they will be corrected
                    double minY = rTree.bounds.getMinY();
                    double maxY = rTree.bounds.getMaxY();
                    minX = rTree.bounds.getMinX();
                    maxX = rTree.bounds.getMaxX();

                    // now we run the parallel loop over otherColumn, from j to whichever row is the closest, in terms of maxY
                    for (; j < otherColumn.length; j++) {
                        double diff = Math.abs(rTree.bounds.getMaxY() - otherColumn[j].bounds.getMaxY());
                        if (minDiff >= diff) {
                            minDiff = diff;
                            handleLeaf(otherColumn[j], newLeaf, escalatedObjects, curLayerZoomLevel);
                            if (otherColumn[j].bounds.getMaxY() > maxY) {
                                maxY = otherColumn[j].bounds.getMaxY();
                            }
                            if (otherColumn[j].bounds.getMinY() < minY) {
                                minY = otherColumn[j].bounds.getMinY();
                            }
                            if (otherColumn[j].bounds.getMinX() < minX) {
                                minX = otherColumn[j].bounds.getMinX();
                            }
                            if (otherColumn[j].bounds.getMaxX() > maxX) {
                                maxX = otherColumn[j].bounds.getMaxX();
                            }
                            continue;
                        }
                        break; // we might not get all of otherColumn's children into newCol, so we'll need to handle that
                    }
                    newCol.add(new RTree(newLeaf.toArray(new RTree[0]), new SerializableRectangle2D(minX, minY, maxX - minX, maxY - minY), escalatedObjects));
                }

                if(j < otherColumn.length) { // iff we didn't combine all the elements of otherColumn. 
                    // we just add all the remaining columns in one new leaf.
                    List<MapObject> escalatedObjects = new ArrayList<>(); // obviously need another container for all the leftover objects


                    RTree[] leftOvers = new RTree[otherColumn.length - j];
                    List<RTree> leftOver = new ArrayList<>(); 
                    minX = otherColumn[j].bounds.getMinX(); 
                    maxX = otherColumn[j].bounds.getMaxX(); 
                    double minY = otherColumn[j].bounds.getMinY(), maxY = otherColumn[j].bounds.getMaxY(); 
                    for(; j < otherColumn.length; j++) {
                        handleLeaf(otherColumn[j], leftOver, escalatedObjects, curLayerZoomLevel);
                        if(otherColumn[j].bounds.getMinX() < minX) { minX = otherColumn[j].bounds.getMinX(); }
                        if(otherColumn[j].bounds.getMinY() < minY) { minY = otherColumn[j].bounds.getMinY(); }
                        if(otherColumn[j].bounds.getMaxX() > maxX) { maxX = otherColumn[j].bounds.getMaxX(); }
                        if(otherColumn[j].bounds.getMaxY() > maxY) { maxY = otherColumn[j].bounds.getMaxY(); }
                    }
                    newCol.add(new RTree(leftOver.toArray(new RTree[0]), new SerializableRectangle2D(minX, minY, maxX - minX, maxY - minY), escalatedObjects));
                }
                newLayer.add(newCol.toArray(new RTree[0]));
            }

            if(activeLayer.length % 2 != 0 ) { // if activeLayer isn't divisible by 2, the last column wasn't combined with anyone
                newLayer.add(activeLayer[i - 1]); // so we just add it straight ahead
            }

            activeLayer = newLayer.toArray(new RTree[0][0]); 
            curLayerZoomLevel += zoomDiffPerLevel; 
        }
        // now we just have a single column, and can just make that the children of root
        int afterEverythingTotalObjects = 0; 
        this.children = new RTree[activeLayer[0].length];
        for(int i = 0; i < activeLayer[0].length; i++) {
            this.children[i] = activeLayer[0][i]; 
            afterEverythingTotalObjects += this.children[i].recursiveSetParent(this);
        }
        this.zoomLevel = 1;
        objects = null; 
    }

    /**
     * Utility function that does the appropriate thing with a given leaf depending on the given parameters.
     * @param leaf is RTree node to do something with. It is added to the newLeafList iff it has a reason to exist, which is having objects 
     * of a lower zoom level than the cutoff. If objects of a higher zoomlevel exist they are added to the escaltedObjectsList, whether the leaf is valid or not
     * @param newLeafList is the list of leafs on which to add this leaf if it has reason to live, otherwise its just destroyed and the list is unchanged
     * @param escalatedObjectsList is the list of objects where any superfluous objects that leaf might have are added. 
     * @param curLayerZoomLevel is the zoom cutoff; all objects in the leaf with a zoomlevel above this are superfluous, and if none with a zoomlevel below this exist, 
     * the leaf has no reason to exist and will be destroyed, with all it's objects added to the escalatedObjectsList
     */
    private static void handleLeaf(RTree leaf, List<RTree> newLeafList, List<MapObject> escalatedObjectsList, double curLayerZoomLevel) {
        List<MapObject> objsToEscalate = null; 
        if(leaf.hasObjectsOfHigherZoomLevel(curLayerZoomLevel)) {
            objsToEscalate = leaf.getAllObjectsAboveCutoff(curLayerZoomLevel); 
            if(objsToEscalate != null) {
                escalatedObjectsList.addAll(objsToEscalate);
            }
        }
        if(leaf.children != null) {
            newLeafList.add(leaf);
        } else if(leaf.getData() != null) {
            escalatedObjectsList.addAll(leaf.getData()); 
        }
    }

    /**
     * A constructor for a pure leaf; a node with no children, and some amount of objects
     * @param minX minimum x coordinate of the bounds of the leaf
     * @param maxX maximum x coordinate of the bounds of the leaf
     * @param minY minimum y coordinate of the bounds of the leaf
     * @param maxY maximum y coordinate of the bounds of the leaf
     * @param objects the objects for this leaf to contain, NOT NULL
     */
    private RTree(double minX, double maxX, double minY, double maxY, List<MapObject> objects) { 

        this.bounds = new SerializableRectangle2D(minX, minY, Math.abs(maxX - minX), Math.abs(maxY - minY));
        this.data = objects; 
        objects.sort(Comparator.comparingDouble(MapObject::getZoomLevel));
        this.children = null;
        this.parent = null; 
        this.zoomLevel = objects.get(objects.size()- 1).getZoomLevel();
    }

    /**
     * A method which runs through the objects of this node, cuts off and returns all above the threshold given, after removing said objects from this object's data. 
     * @param zoomCutoff is the cutoff under which the objects stay in this node, and over which they are returned
     * @return a list of the objects that are above the zoomCutoff
     */
    private List<MapObject> getAllObjectsAboveCutoff(double zoomCutoff) {
        if(data == null) { return null; } // return an empty list
        int cutoff = 0;
        for(; cutoff < data.size(); cutoff++) {
            if(data.get(cutoff).getZoomLevel() > zoomCutoff) { break; }
        }
        if(cutoff == data.size()) /*this means that there are no too small objects*/ { return null; }
        List<MapObject> newObjects = data.subList(cutoff, data.size());
        data = data.subList(0, cutoff); // toIndex is exclusive
        zoomLevel = data.get(data.size() - 1).getZoomLevel();
        return newObjects;
    }

    /**
     * A function which runs over the data and creates a new child node for all the objects that have an identical zoom level.
     * These are then added to the children array of this node 
     */
    private void seperateObjectsIntoIdenticalZoomLevelClumps() {
        if(data == null || data.size() < 2) { return; }
        
        List<RTree> childrenToAdd = new ArrayList<>(); 
        while(data.get(data.size() - 1).getZoomLevel() > data.get(0).getZoomLevel()) { // if that isn't a final level
            List<MapObject> midLevelObjects = getLowestZoomLevelObjects(); 
            
            RTree midLevelChild = new RTree(children,  midLevelObjects);
            childrenToAdd.add(midLevelChild); 
        }
        childrenToAdd.addAll(Arrays.asList(children));
        children = childrenToAdd.toArray(new RTree[0]);
    } 

    /**
     * Constructor for a node that will find its own bounds from the provided objects
     * @param children the children, can be null
     * @param midLevelObjects the objects, cannot be null since they are needed to find the bounds 
     */
    private RTree(RTree[] children, List<MapObject> midLevelObjects) {
        double minX = midLevelObjects.get(0).getBounds().getMinX();
        double minY = midLevelObjects.get(0).getBounds().getMinY();
        double maxX = midLevelObjects.get(0).getBounds().getMaxX();
        double maxY = midLevelObjects.get(0).getBounds().getMaxY();
        for(int i = 1; i < midLevelObjects.size(); i++) {
            if(midLevelObjects.get(i).getBounds().getMinX() < minX) { minX = midLevelObjects.get(i).getBounds().getMinX(); }
            if(midLevelObjects.get(i).getBounds().getMinY() < minY) { minY = midLevelObjects.get(i).getBounds().getMinY(); }
            if(midLevelObjects.get(i).getBounds().getMaxX() > maxX) { maxX = midLevelObjects.get(i).getBounds().getMaxX(); }
            if(midLevelObjects.get(i).getBounds().getMaxY() > maxY) { maxY = midLevelObjects.get(i).getBounds().getMaxY(); }
        }
        this.bounds = new SerializableRectangle2D(minX, minY, maxX - minX, maxY - minY);
        this.children = children;
        this.data = midLevelObjects;
        this.zoomLevel = this.data.get(0).getZoomLevel();
    }

    /**
     * A function to find the index at which that object and the first object have different zoom-levels.
     * When the given list is sorted, this allows us to split it into a list of objects with the lowest zoomLevel < index, and the rest of the objects > index
     * Takes linear time in the size of the list
     * @param list the list of MapObjects, assumed to be sorted according to zoom level
     * @return the index at which a different zoom level appears. Naturally, if the zoomLevels are all identical, this will be equal to list.size()
     */
    private static int getZoomDiffCutoff(List<MapObject> list) {
        int cutoff = 1;
        double firstZoomLevel = list.get(0).getZoomLevel();
        for(; cutoff < list.size(); cutoff++) {
            if(list.get(cutoff).getZoomLevel() > firstZoomLevel) { break; }
        } 
        return cutoff; 
    }

    /**
     * A method  which removes the lowest zoom level elements from the given list, and returns them in a seperate list
     * @return a list of all the objects whose zoomlevels are the smallest in the list. If all zoomlevels are identical it's just 
     * the full list of objects
     */
    private List<MapObject> getLowestZoomLevelObjects() {
        int cutoff = getZoomDiffCutoff(data); 
        if(cutoff == data.size()) {
            return data; 
        }
        List<MapObject> newObjects = data.subList(0, cutoff);
        data = data.subList(cutoff, data.size());
        return newObjects; 
    }

    /**
     * A method that finds out if this node has at least one object that is above the cutoff
     * @param zoomLevelCutoff the cutoff above which objects aren't valid
     * @return if this node has at least one object that is below the cutoff
     */
    private Boolean hasObjectsOfHigherZoomLevel(double zoomLevelCutoff) {
        if(data == null) { return false; } // has neither children nor any data to even check against the cutoff
        return !(data.get(0).getZoomLevel() > zoomLevelCutoff);
    }

    /**
     * Constructor for a regular node, one that might have children and might have objects
     * @param children a, possibly null, array of children that this node is the parent to 
     * @param bounds the bounds of this node. Note that this should include all the children's bounds
     * @param objects a, possibly null, list of objects. IF it isn't null the list will be sorted according to zoomlevel,
     * to support a variety of operations according to that. 
     */
    private RTree(RTree[] children, SerializableRectangle2D bounds, List<MapObject> objects) { // branch
        this.parent = null; 
        this.children = children; 
        this.bounds = bounds;
        if(objects == null || objects.size() == 0) { this.data = null; this.zoomLevel = 1; }
        else {
            objects.sort(Comparator.comparingDouble(MapObject::getZoomLevel));
            this.zoomLevel = objects.get(objects.size() - 1).getZoomLevel();
            this.data = objects;
            if(objects.get(objects.size() - 1).getZoomLevel() > objects.get(0).getZoomLevel()) {
                seperateObjectsIntoIdenticalZoomLevelClumps();
            }
        }
    }

    /**
     * A simple method that sets THIS node's parent to the given, and then loops over all it's children, setting their parent to THIS. 
     * The same is then done for their children, and in that manner the entire tree gets correct parents. 
     * Also counts all the objects in the tree, for debugging purposes.
     * @param parent the parent of THIS node
     * @return the total amount of objects in this tree
     */
    private int recursiveSetParent(RTree parent) {
        this.parent = parent; 
        int thisCount = 0; 
        if(data != null) { thisCount = data.size(); }
        if(children == null) { return thisCount; }
        for(RTree child : children) {
            thisCount += child.recursiveSetParent(this); 
        }
        return thisCount;
    }

    /**
     * Simple method to check 
     * @param view the given node to check for intersection with 
     * @return whether the bounds of this node intersect the given
     */
    public Boolean inBounds(SerializableRectangle2D view) {
        return view.intersects(bounds);
    }

    
    /**
     * A recursive method which first decides if this node should be visible, based on the given bounds and zoomlevel. 
     * If it should be visible, it adds all of its objects to the given list, and then calls the same for all its children 
     * @param view the view against which to check this node's bounds
     * @param objects the list of objects to which these nodes objects will be added if its valid
     * @param zoomLvl the zoomlevel to compare with these nodes zoomlevel. If it's larger than the nodes own zoomlevel, it shouldn't be drawn
     */
    public void getObjectsInSquare(SerializableRectangle2D view, List<MapObject> objects, double zoomLvl) {
        if(!inBounds(view) || zoomLvl > zoomLevel) { return; }
        
        if(data != null) {
            objects.addAll(data);
        }

        if(children == null) { return; }

        for(RTree child : children) {
            child.getObjectsInSquare(view, objects, zoomLvl);
        }
    }

    /**
     * Returns a list of MapObjects.
     * @return the list of MapObjects
     */
    public List<MapObject> getData() { return data; }

    /**
     * Calculates and returns the best distance between the reference point and other points on the map.
     * @param refPoint the reference point used to calculate the distance
     * @return the best distance between the reference point and other points on the map
     */
    public double getBestDistance(SerializablePoint2D refPoint) {
        return getBestDistance(refPoint.getX(), refPoint.getY()); 
    }

    /**
     * Finds the smallest distance of the sides of this node to the given point.
     * With the stipulation that a point inside the rectangle has a distance of 0.
     * @param x x-coordinate of the point
     * @param y y-coordinate of the point
     * @return the smallest distance between the given point and this, at least 0 
     */
    public double getBestDistance(double x, double y) {
        double xDiff = (x >= bounds.getMinX() && x <= bounds.getMaxX()) ? 0 : Math.min(Math.abs(bounds.getMinX() - x), Math.abs(bounds.getMaxX() - x));
        double yDiff = (y >= bounds.getMinY() && y <= bounds.getMaxY()) ? 0 : Math.min(Math.abs(bounds.getMinY() - y), Math.abs(bounds.getMaxY() - y));
        return Math.sqrt(Math.pow(xDiff, 2) + Math.pow(yDiff, 2));
    }

    /**
     * Returns the nearest neighbour to the given point.
     * @param point the point to find the nearest neighbour for
     * @return the nearest neighbour to the given point
     */
    public MapObject getNearestNeighbour(SerializablePoint2D point) {
        return getNearestNeighbour(point.getX(), point.getY());
    }
    /**
     * Returns the nearest neighbour to the given point with the given coordinates.
     * @param x the x-coordinate of the point
     * @param y the y-coordinate of the point
     * @return the nearest neighbour to the given point
     */
    public MapObject getNearestNeighbour(double x, double y) {
       List<MapObject> objectsInLeaf = new ArrayList<>();
       getObjectsInSameLeaf(objectsInLeaf, x, y);
       MapObject objectWithLeastDistance = null;
       double leastDistance = Double.POSITIVE_INFINITY;

       for(MapObject object : objectsInLeaf) {
           double[] coordinates = object.coordinates;
           for(int i = 0; i < coordinates.length; i += 2) {
               if(calculateDistance(x, y, coordinates[i], coordinates[i+1]) < leastDistance) {
                   objectWithLeastDistance = object;
                   leastDistance = calculateDistance(x, y, coordinates[i], coordinates[i+1]);
               }
           }
       }
       return objectWithLeastDistance;
    }


    /**
    * This method recursively gets {@link MapObject}s that are in the same leaf as the given point.
     * If the point is not within the bounds of this node, or if the node has no data and children == null, this method returns itself.
     * If the point is within the bounds of this node and the node has data, the data in the node is added to the given list.
     * The method then calls itself on all the children of this node.
     * @param objects the list to which all the {@link MapObject}s in the same leaf as the point will be added
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     */
    private void getObjectsInSameLeaf(List<MapObject> objects, double x, double y) {
        if(getBestDistance(x, y) != 0) { return; }

        if(data != null) {
            objects.addAll(data);
        }

        if(children == null) { return; }

        for(RTree child : children) {
            child.getObjectsInSameLeaf(objects, x, y);
        }
    }

    /**
     * This function finds nearest road from a Serializable Point2D
     * @param point, the point consisting of X and Y that the function finds nearest road for
     * @return the nearest road to point.get(X) and point.getY()
     */
    public Highway getNearestRoad(SerializablePoint2D point) {
        return getNearestRoad(point.getX(), point.getY());
    }

    /**
     * Returns the nearest {@link Highway} to the point (x, y).
     * This method gets all the {@link Highway}s in the same leaf as the point using {@link #getHighwaysInSameLeaf(List, double, double)}.
     * It then itereates over {@link Highway}s to find the one with the least distance to the point.
     * @param x the x coordinate of the point
     * @param y the y coordinate of the point
     * @return the nearest {@link Highway} to the given point
     */
    public Highway getNearestRoad(double x, double y) {
        List<Highway> highwaysInLeaf = new ArrayList<>();
        getHighwaysInSameLeaf(highwaysInLeaf, x, y);
        Highway objectWithLeastDistance = null;
        double leastDistance = Double.POSITIVE_INFINITY;

        for(Highway object : highwaysInLeaf) {
            double[] coordinates = object.coordinates;
            for(int i = 0; i < coordinates.length; i += 2) {
                if(calculateDistance(x, y, coordinates[i], coordinates[i+1]) < leastDistance) {
                    objectWithLeastDistance = object;
                    leastDistance = calculateDistance(x, y, coordinates[i], coordinates[i+1]);
                }
            }
        }
        return objectWithLeastDistance;
    }

    /**
     * Helper method using recursion to fill the given List with all Highway objects that is in the same leaf as the given coordinates.
     * If the distance of the current node to the coordinates is not 0, the method returns without adding any Highway objects to the List.
     * If the current node contains data, the method adds any Highway objects in the data to the List.
     * If the current node has children, then the method calls itself on each child node and continues searching for Highway objects.
     * @param objects the List to be filled with Highway objects in the same leaf as the double x and double y coordinates
     * @param x the x-coordinate of the point being searched for
     * @param y the y-coordinate of the point being searched for
     */
    private void getHighwaysInSameLeaf(List<Highway> objects, double x, double y) {
        if(getBestDistance(x, y) != 0) { return; }

        if(data != null) {
            for(MapObject object : data) {
                if(object instanceof Highway) objects.add((Highway) object);
            }
        }

        if(children == null) { return; }

        for(RTree child : children) {
            child.getHighwaysInSameLeaf(objects, x, y);
        }
    }

    /**
     * Calculates the Euclidean distance between two points, specified by their x and y coordinates.
     * @param fromX the x-coordinate of the starting point that we need to calculate from
     * @param fromY the y-coordinate of the starting point that we need to calculate from
     * @param toX the x-coordinate of the ending point that we need to find the distance to
     * @param toY the y coordinate of the ending point that we need to find the distance to
     * @return the euclidean distance between the two points
     */
    private double calculateDistance(double fromX, double fromY, double toX, double toY) {
        return Math.sqrt(Math.pow((fromX - toX), 2) + Math.pow((fromY - toY), 2));
    }

    /**
     Returns the bounds of this RTree node as a SerializableRectangle2D object.
     @return the bounds of this RTree node
     */
    public SerializableRectangle2D getBounds() {
        return bounds;
    }

    /**
     Returns the children of this RTree node as an array of RTree objects.
     @return the children of this RTree node
     */
    public RTree[] getChildren() { return children; }

    /**
     * Used to visualize nodes, by drawing a red outline around nodes with children, and a blue outline around pure nodes. 
     * Only draws nodes that should be visible 
     * @param gc graphicsContext on which the outlines will be drawn 
     * @param view the view to check the nodes bounds against 
     * @param zoomLvl the zoomlevel to compare with the nodes 
     */
    public void debugOutline(GraphicsContext gc, SerializableRectangle2D view, double zoomLvl) {
        if(!inBounds(view) || zoomLvl > zoomLevel) { return; }

        if(data == null) {
            gc.setStroke(Color.BLUE);
        } else {
            gc.setStroke(Color.RED);
            gc.strokeRect(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
        if(children == null) {
            return; 
        }
        for(RTree chud : children) {
            chud.debugOutline(gc, view, zoomLvl);
        }
    }
}

