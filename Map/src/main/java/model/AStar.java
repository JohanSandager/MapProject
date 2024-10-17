package model;

/**
 * This class extends the Dijkstra class and makes it into the AStar algorithm
 * A* looks for the best path by using a heuristic function,
 * prioritizing the nodes that should be better than other nodes, instead of just exploring all possible ways.
 */
public class AStar extends Dijkstra{


    /**
     * Constructor for A Star algorithm. It uuses the Dijkstra constructor with graph, source, target, max speed and permission.
     * @param graph, the graph on which the algorithm should be performed
     * @param source the source that the shortest path needs to be calculated FROM
     * @param target, the destination target that the shortest path needs to be calculated TO
     * @param maximumSpeed, the maxiumspeed on roads, so the shortest path can be found
     * @param permission, which permission is on certain road, e.g bicycle only, car only, etc.
     */
    public AStar(Graph graph, int source, int target, double maximumSpeed, Graph.TravelPermission permission) {
        super(graph, source, target, maximumSpeed, permission);
    }

    /**
     * Relax method to relax edges. Maintains a minPQ data structure in order to relax edges in order to maintain the shortest path
     * from the source to the target node.
     * If the distace to a certain vertex is larger than the distance from a certain vertex plus it's weight,
     * the distanceTo the index of the vertex to is set to be equal to the distanceTo's vertexFrom-vertex plus its weight.
     * The edgeTo of the vertex to is then set to be equal to the edge
     * And the minimumPQ inserts or replaces (if there is already found a path) the verxTo edge.ToNode at the index of the vertexTo
     * @param edge, edge in the graph
     */
    @Override
    protected void relax(Graph.Edge edge) {
        int vertexFrom = edge.fromNode, vertexTo = edge.toNode;
        double weight = edge.getTravelTimeInMinutes(maximumSpeed) + graph.calculateEdgeDistance(edge.fromNode, target);
        if(distanceTo[vertexTo] > distanceTo[vertexFrom] + weight) {
            distanceTo[vertexTo] = distanceTo[vertexFrom] + weight;
            edgeTo[vertexTo] = edge;
            minimumPQ.insertOrReplace(distanceTo[vertexTo], vertexTo);
        }
    }
}
