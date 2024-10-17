package model.DataTypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * This represents a KD-tree of 2 dimensional points with geometric coordinates. 
 * It supports nearest neighbour queries in average 5*log2(n) time. 
 * It does this via an internal Node class, which naturally means that it takes up quite a bit of space, 
 * which is why its only used after we have culled away a lot of the nodes. 
 * It takes an input of DoublePointHashTable, since that is the structure we store the nodes of the Graph in. 
 * Together, these two classes provide support for the queries that the Graph class needs. 
 */
public class DPHT2DTree implements Serializable{
    private Node root; 
    
    private static final Comparator<Node> xWayComp = Comparator.comparingDouble((Node a) -> a.x);
    private static final Comparator<Node> yWayComp = Comparator.comparingDouble((Node a) -> a.y);
    
    /**
     * The internal Node class, which represents a binary tree node in that it has two children and a parent, any of whom may be null. 
     * It also has a particular ID, which is useful in our particular case since the points are identificed and accessed by ID. 
     * We have the heuteristic that all the elements in the left child are less than or equal to this, and all the elements in the right 
     * child are greater than or equal to. 
     */
    private class Node {
        double x; 
        double y;
        int id;  
        Node rChild; 
        Node lChild; 
        Node parent; 
        
        public Node(double x, double y, int id) {
            this.x = x; 
            this.y = y; 
            this.id = id; 
        }
    }
    
    /**
     * Constructor that generates the corrosponding 2D point tree from the given hashtable. 
     * It does this via a divide and conquer approach, in which the list of nodes are sorted by their x-coordinates. 
     * Then, the median of this sorted list becomes the root node, and the process is repeated for the upper and lower half, 
     * only this time theyre sorted by y. The roots of those subtrees become the children of the root. 
     * In this recursive manner, the entire tree is constructed. This will theoretically take (log2(n)*n)log2(n) time. 
     * @param initTable the table to construct the tree from 
     */
    public DPHT2DTree(DoublePointHashTable initTable) {
        ArrayList<Node> nodes = new ArrayList<>(initTable.size()); 
        for(int i = 0; i < initTable.size(); i++) {
            nodes.add(new Node(initTable.getXFromID(i), initTable.getYFromID(i), i)); 
        }
        
        nodes.sort(xWayComp); 
        int medianI = nodes.size() / 2; 
        Node medianNode = nodes.get(medianI); 
        root = medianNode; 
        List<Node> left = nodes.subList(0, medianI); 
        List<Node> right = nodes.subList(medianI + 1, nodes.size()); 
        
        root.lChild = createNodes(left, false, root); 
        root.rChild = createNodes(right, false, root);
        
        System.out.println("Root is: " + root.id);
        
        StringBuilder treeStr = new StringBuilder(); 
    }
    

    /**
     * The recursive function that we use to create the tree. Each call to it takes log2(n)*n, with n being the size of nodes. 
     * It calls itself on continually halfed lists untill it cannot be halfed anymore. 
     * In other words, the each returned value from this method is a valid binary tree. As this invariant is maintained all the way up 
     * the recursion chain, we can guarantee that we end up with two full binary trees as children of the root, and thus we make a binary tree. 
     * @param nodes the list of nodes 
     * @param xWay whether to compare along the x or y axis 
     * @param parent the parent of whom we create a child 
     * @return the root node of a binary tree constructed from the given nodes. 
     */
    private Node createNodes(List<Node> nodes, boolean xWay, Node parent) {
        Node medianNode; 
        if(nodes.size() == 0) { return null; }
        else if(nodes.size() == 1) { medianNode = nodes.get(0); medianNode.parent = parent; return medianNode; }
        
        nodes.sort(xWay ? xWayComp : yWayComp); 
        
        int medianI = nodes.size() / 2; 
        medianNode = nodes.get(medianI);  
        medianNode.parent = parent; 
        
        List<Node> left = nodes.subList(0, medianI); 
        List<Node> right = nodes.subList(medianI + 1, nodes.size()); 
        
        medianNode.lChild = createNodes(left, !xWay, medianNode); 
        medianNode.rChild = createNodes(right, !xWay, medianNode);
        
        return medianNode; 
    }

    // PERFORMANCE TESTING
    private int amtNodesExamined; 
    public int getNodesExamined() { return 0; }
    // PERFORMANCE TESTING
    
    /**
     * A nearest neighbour query, which gives the ID of the neareast neighbour found, since that's what this particular client uses. 
     * The particular point can be retrieved through the DoublePointHashTable used to construct this tree. 
     * Experimentally this takes on average ~5log(n) time. Theoretically its somewhere between log(n) and n. 
     * @param x the x-coordinate of the nearest point 
     * @param y the y-coordinate of the nearest point 
     * @return the ID of the point in the tree that is the nearest to the given coordinates. 
     * s
     * The search is done by maintaining a stack of points to examine. For each node, we first examine whether our point given by (x,y) 
     * would be to the left or right of it. We always go on to repeat the process for the subtree that our point would be in, and we also 
     * look at the other subtree, if there could be a closer point in there than the current best. As aforementioned this is done in a 
     * recursive-ish manner, by maintaining a stack of points to look at. 
     * This means we "depth first search" all the way to the position that our point would be inserted in, which is pretty likely to be either 
     * the closest point, or pretty close to it, which will rule out a lot more checks further up in the stack. 
     * Naturally we'll always run atleast 2*log(n), since both children of root are always added. Could maybe optimize this bound lower since its 
     * often a pointless traversal. Could prolly be done with some comparing the ideal distance between point to add to the stack and our point to 
     * the best distance, however implementation proved a little hairy. 
     */
    public int getClosestNodeID(double x, double y) {
        Node curNode = root; 
        boolean xWay = true;

        double currentBest = Double.POSITIVE_INFINITY; 
        GoodStack<Node> stack = new GoodStack<>(); 
        GoodStack<Boolean> pStack = new GoodStack<>(); 
        GoodStack<Boolean> xWayStack = new GoodStack<>(); 
        stack.push(curNode);
        pStack.push(true);
        xWayStack.push(xWay);
        int bestDistID = -1;
        
        while(stack.hasNext()) {
            xWay = xWayStack.pop();
            curNode = stack.pop(); 
            double distance =  Math.pow(curNode.x - x,2) + Math.pow(curNode.y - y,2); 
            if(distance < currentBest) { currentBest = distance; bestDistID = curNode.id; }
            // find out whether this is left or right of the splitline 
            boolean left = xWay ? x < curNode.x : y < curNode.y; 
            
            double splitLine = Math.pow(xWay ? curNode.x - x : curNode.y - y, 2); 
            if(splitLine > currentBest) { // there could only be a closer point in this subtree. Closest point other is (x||y, splitLine), so if bestDist < splitLine
                Node child = left ? curNode.lChild : curNode.rChild; 
                
                pushChildIfValid(x, y, child, stack, xWayStack, xWay, currentBest);
            } else { // we know the closest point is either in this subtree or already found 
                if(left) {
                    pushChildIfValid(x, y, curNode.rChild, stack, xWayStack, xWay, currentBest);
                    pushChildIfValid(x, y, curNode.lChild, stack, xWayStack, xWay, currentBest );
                } else {
                    pushChildIfValid(x, y, curNode.lChild, stack, xWayStack, xWay, currentBest);
                    pushChildIfValid(x, y, curNode.rChild, stack, xWayStack, xWay, currentBest);
                }
            }
        }
        return bestDistID; 
    }
    

    /**
     * A utility function to perform the search, by allowing us to just pass everything. 
     * @param x the x-coordinate of the point we're trying to find a nearest neighbour to 
     * @param y the y-coordinate of the point we're trying to find a nearest neighbour to 
     * @param child the child to push if we find it valid
     * @param nodeStack the stack of nodes onto which to push the child if valid
     * @param xWayStack the stack of directions on which to push 
     * @param xWay the current direction 
     * @param currentBest
     */
    private void pushChildIfValid(double x, double y, Node child, GoodStack<Node> nodeStack, GoodStack<Boolean> xWayStack, boolean xWay, double currentBest) {
        if(child == null) { return; }
        nodeStack.push(child); 
        xWayStack.push(!xWay);
    }
}
