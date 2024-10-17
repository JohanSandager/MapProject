package model;

import model.DataTypes.DPHT2DTree;
import model.DataTypes.DoubleArrayList;
import model.DataTypes.DoublePointHashTable;
import model.DataTypes.LongToIntMap;

import java.io.Serializable;
import java.util.*;

/**
 * This is our Graph implementation, the point of which is to create a graph that can be used to do searches with Dijkstra.
 * It consists of edges, which represents roads, paths or other travelable entity, and the intersection of these are represented as nodes.
 * */
public class Graph implements Serializable{
    public enum TravelPermission {
        walkable,
        cyclable,
        drivable
    }

    /**
     * An enum for the different permutations of permissions for a particular road, excluding no permissions(since that never happens)
     * The permissions are ordered, such that driving > cycling > walking in terms of values. SO for example all permissions are the highest, 
     * only walk is "onlyWalkable, onlyCyclable, walkableCycable, onlyDrivable, walkableDrivable, drivableCyclable, all"
     * THe current order is All >
     */
    public enum roadPermissions {
        onlyWalkable, onlyCyclable, walkableCycable, onlyDrivable, walkableDrivable, drivableCyclable, all
    }

    DoublePointHashTable coordinates;
    DPHT2DTree bestDistTree;
    LongToIntMap nodeIdToNodeCoordsIndex;
    List<Edge> edges;
    List<List<Edge>> adjacencyLists;
    int createdNodesCount; 

    public Graph() {
        bestDistTree = null;
        nodeIdToNodeCoordsIndex = new LongToIntMap(); 
        edges = new ArrayList<>();
        adjacencyLists = new ArrayList<>();

        coordinates = new DoublePointHashTable();
        createdNodesCount = 0;
    }

    public DoubleArrayList getXCoords() { return coordinates.getXCoordsList(); }
    public DoubleArrayList getYCoords() { return coordinates.getYCoordsList(); }
    public List<Edge> getEdges() { return edges; }


    /**
     * @param nodeX the x coordinate of the node
     * @param nodeY the y coordinate of the node
     * @return the index on which the coordinate was added
     */
    public int addNode(double nodeX, double nodeY) {
        adjacencyLists.add(new LinkedList<>());
        createdNodesCount++; 
        return coordinates.add(nodeX, nodeY);
    }

    /**
     * @param fromNodeID the id of the node that begins the edge
     * @param toNodeID the id of the node that terminates the edge
     * @param addedDistance the distance of the collapsed edge
     * @param speedLimit the speed limit of the edge
     * @param highway the highway that the edge is a simplification of
     * @param permission what vehicle may travel on the road
     */
    public void addEdge(int fromNodeID, int toNodeID, double addedDistance, double speedLimit, Highway highway, roadPermissions permission) {
        double distance = calculateEdgeDistance(coordinates.getXFromID(fromNodeID), coordinates.getYFromID(fromNodeID),
        coordinates.getXFromID(toNodeID), coordinates.getYFromID(toNodeID)) + addedDistance;
        Edge edge = new Edge(fromNodeID, toNodeID, distance, speedLimit, highway);

        edges.add(edge); 
        adjacencyLists.get(fromNodeID).add(edge);

        if(permission.ordinal() < roadPermissions.onlyDrivable.ordinal()) { // we can't drive here
            if(permission.ordinal() < roadPermissions.onlyCyclable.ordinal()) { // only walkable 
                edge.setPermission(TravelPermission.walkable, true);
            } else { // walkable cycleable, since it's not driveable
                edge.setPermission(TravelPermission.walkable, true);
                edge.setPermission(TravelPermission.cyclable, true);
            }
        } else {
            edge.setPermission(TravelPermission.drivable, true);
            if(permission == roadPermissions.walkableDrivable) {
                edge.setPermission(TravelPermission.walkable, true);
            } else if (permission == roadPermissions.drivableCyclable) {
                edge.setPermission(TravelPermission.cyclable, true);
            } else {
                edge.setPermission(TravelPermission.cyclable, true);
                edge.setPermission(TravelPermission.walkable, true);
            }
        }
    }

    public int getNodeID(double nodeX, double nodeY) {
        return coordinates.findID(nodeX, nodeY);
    }

    public int getSmallestDistanceNodeID(double x, double y) {
        if(bestDistTree == null) { bestDistTree = new DPHT2DTree(coordinates); }
        return bestDistTree.getClosestNodeID(x, y);
    }

    //method for memory cleanup 
    public void rmBestDistTree() {
        bestDistTree = null; 
    }

    public double calculateEdgeDistance(int fromNodeID, int toNodeID) {
        return calculateEdgeDistance(coordinates.getXFromID(fromNodeID),coordinates.getYFromID(fromNodeID), coordinates.getXFromID(toNodeID), coordinates.getYFromID(toNodeID));
    }

    /**
     * Calculates the length of an edge in map coordinates
     * @return the distance between the two points
     */
    public double calculateEdgeDistance(double fromNodeX, double fromNodeY, double toNodeX, double toNodeY) {
        return Math.sqrt(Math.pow((fromNodeX - toNodeX), 2) + Math.pow((fromNodeY - toNodeY), 2));
    }

    public Edge[] getAdjacentNodes(int index) {
        return adjacencyLists.get(index).toArray(new Edge[0]);
    }

    public int nodeSize() {
        return createdNodesCount;
    }

    public double getXFromID(int id) {
        return coordinates.getXFromID(id);
    }

    public double getYFromID(int id) {
        return coordinates.getYFromID(id);
    }

    /**
     * Mathematical formula was found <a href="https://www.movable-type.co.uk/scripts/latlong.html">here</a>.
     * Calculates the distance in meters from two points using map coordinates.
     * @param fromX from coordinate in Latitude
     * @param fromY from coordinate in Longitude
     * @param toX to coordinate in Latitude
     * @param toY to coordinate in Longitude
     * */
    public static double getDistanceInMetersFromCoordinates(double fromX, double fromY, double toX, double toY) {
        double R = 6371; //Earth radius
        double feta_1 = fromX * Math.PI / 180; //In Radians
        double feta_2 = toX * Math.PI / 180; //In Radians
        double deltaFeta = (toX - fromX) * Math.PI / 180; //In Radians
        double deltaLambda = (toY - fromY) * Math.PI / 180; //In Radians

        double a = Math.pow(Math.sin(deltaFeta / 2), 2) +
                Math.pow(Math.sin(deltaLambda / 2), 2) *
                        Math.cos(feta_1) * Math.cos(feta_2);
        double c = 2 * Math.asin(Math.sqrt(a));

        return R * c;
    }

    public static class Edge implements Serializable {
        public boolean[] permissions;
        public int fromNode;
        public int toNode;
        public double distance;
        public double speedLimit;
        public Highway highway;

        public Edge(int fromNode, int toNode, double distance, double speedLimit, Highway highway) {
            permissions = new boolean[TravelPermission.values().length];
            Arrays.fill(permissions, false);
            this.fromNode = fromNode;
            this.toNode = toNode;
            this.distance = distance;
            this.speedLimit = speedLimit;
            this.highway = highway;
        }

        /**
         * Sets the permissions value of the TravelPermission enum for the edge
         * */
        public void setPermission(TravelPermission permission, boolean value) {
            permissions[permission.ordinal()] = value;
        }

        public boolean getPermission(TravelPermission permission) {
            return permissions[permission.ordinal()];
        }

        /**
         * @param speed speed measured in Kilometer per Hour
         * @return The travel time in kilometer per hour, based on the passed speed
         * */

        public double getTravelTimeInMinutes(double speed) {
            if(speed > speedLimit) speed = speedLimit;
            return (distance / speed) / 60;
        }
    }
}
