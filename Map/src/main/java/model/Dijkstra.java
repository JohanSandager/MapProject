package model;

import model.DataTypes.DoubleIndexMinPQ;
import model.DataTypes.GoodStack;

import java.util.*;

/**
 * The {@code Dijkstra} class performs Dijkstra's algorithm on a given graph, stopping the search when the shortest path has been found
 * */
public class Dijkstra {
    protected Graph graph;
    protected int source;
    protected int target;

    protected Graph.TravelPermission permission;

    protected double[] distanceTo;
    protected Graph.Edge[] edgeTo;
    protected DoubleIndexMinPQ minimumPQ;
    protected double maximumSpeed;

    /**
     * The constructor for our implementation of Dijkstra
     * @param graph the graph on which to perform the Dijkstra
     * @param source the id of the node in the graph that should be the source of the search
     * @param target the id of the node in the graph that should be the target of the search
     * @param maximumSpeed the speed limitation of the mode of transport
     * @param permission the required permissions for the search
     */
    public Dijkstra(Graph graph, int source, int target, double maximumSpeed, Graph.TravelPermission permission) {
        this.graph = graph;
        this.source = source;
        this.target = target;
        this.permission = permission;
        distanceTo = new double[graph.nodeSize()];
        edgeTo = new Graph.Edge[graph.nodeSize()];
        minimumPQ = new DoubleIndexMinPQ(graph.nodeSize());
        this.maximumSpeed = maximumSpeed;

        performSearch();
    }

    protected void performSearch() {
        Arrays.fill(distanceTo, Double.POSITIVE_INFINITY);
        distanceTo[source] = 0.0;

        minimumPQ.insert(0, source);
        while(!minimumPQ.isEmpty()) {
            int curVertex = minimumPQ.delMinIndex(); 
            for(Graph.Edge edge : graph.getAdjacentNodes(curVertex)) {
                if (!edge.getPermission(permission)) {
                    continue;
                }
                relax(edge);
                if(edge.toNode == target) {
                    return; // we did our job
                }
            }
        }
    }

    /**
     * Method used to relax an edge
     * @param edge the edge to relax
     */
    protected void relax(Graph.Edge edge) {
        int vertexFrom = edge.fromNode, vertexTo = edge.toNode;
        double weight = edge.getTravelTimeInMinutes(maximumSpeed);
        if(distanceTo[vertexTo] > distanceTo[vertexFrom] + weight) {
            distanceTo[vertexTo] = distanceTo[vertexFrom] + weight;
            edgeTo[vertexTo] = edge;
            minimumPQ.insertOrReplace(distanceTo[vertexTo], vertexTo);
        }
    }

    /**
     * @return the path of edges contained in the shortest route
     */
    public Iterable<Graph.Edge> getPath() {
        GoodStack<Graph.Edge> path = new GoodStack<>();
        for(Graph.Edge edge = edgeTo[target]; edge != null; edge = edgeTo[edge.fromNode]) {
            path.push(edge);
        }
        return path;
    }
}
