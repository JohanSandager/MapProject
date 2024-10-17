package model;

import java.util.List;
import java.util.Map;

/**
 * A Way represents a collection of nodes forming a path or route on a map.
 *It extends the MapElement class and contains an array of nodes and a boolean, indicating whether the road is closed or not.
 */
public class Way extends OSMElement {

    private Node[] nodes;
    private boolean isClosedWay;

    /**
     * Constructs a Way object with the specified list of nodes and tags.
     *
     * @param nodes a list of nodes forming the way
     * @param tags a map of tags describing the way
     */
    public Way(List<Node> nodes, Map<String, String> tags) {
        this.nodes = copyValues(nodes);
        this.tags = tags;
        isClosedWay = isClosedWay();
    }
    /**
     * Determines whether the way is closed or not based on the first and last nodes.
     *
     * @return true if the way is closed, false otherwise
     */
    private boolean isClosedWay() {
        Node firstNodeId = nodes[0];
        Node lastNodeId = nodes[nodes.length - 1];
        if(firstNodeId == lastNodeId) return true;
        return false;
    }
    /**
     * Returns a boolean indicating whether the way is closed or not.
     *
     * @return true if the way is closed, false otherwise
     */
    public boolean getIsClosedWay() {
        return isClosedWay;
    }

    /**
     * Returns an array of nodest that make up the way
     *
     * @return an array of nodes
     */
    public Node[] getNodes() {
        return nodes;
    }

    /**
     * Copies the values from the specified list of nodes to an array.
     *
     * @param nodes a list of nodes to copy from
     * @return an array of nodes with the same values as the input list
     */
    private Node[] copyValues(List<Node> nodes) {
        Node[] tempNodeArray = new Node[nodes.size()];
        for(int i = 0; i < nodes.size(); i++) {
            tempNodeArray[i] = nodes.get(i);
        }
        return tempNodeArray;
    }
}
