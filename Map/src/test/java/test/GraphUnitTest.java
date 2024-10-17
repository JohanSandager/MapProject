package test;

import model.Graph;
import model.Graph.roadPermissions;
import model.Graph;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

public class GraphUnitTest {
    private Graph graph; 
    private double[] xCoords = { 1.0 ,2.0 ,3.0 ,4.0 ,5.0 ,6.0 ,7.0 ,8.0 ,9.0 , 10.0}; 
    private double[] yCoords = { 1.0 ,2.0 ,3.0 ,4.0 ,5.0 ,6.0 ,7.0 ,8.0 ,9.0 , 10.0}; 

    @BeforeEach void setup() {
        graph = new Graph(); 
    }

    @Test void edgeCreation() {
        for(int i = 0; i < xCoords.length; i++) {
            graph.addNode(xCoords[i], yCoords[i]);
        }
        for(int i = 0; i < xCoords.length; i++) {
            for(int k = i + 1; k < xCoords.length; k++) {
                graph.addEdge(i,k,0, 1, null, roadPermissions.all);
            }
        }

        Graph.Edge[] firstEdges = graph.getAdjacentNodes(0);
        assertEquals(9, firstEdges.length);  
        Graph.Edge[] lastEdges = graph.getAdjacentNodes(9); 
        assertEquals(0, lastEdges.length); 
        
        int[] countOccurrences = { 0,0,0,0,0,0,0,0,0,0};
        for(int i = 0; i < firstEdges.length; i++) {
            assertEquals(0, firstEdges[i].fromNode);
            countOccurrences[firstEdges[i].toNode]++; 
        }
        assertEquals(0, countOccurrences[0]); // no edge to itself
        for(int i = 1; i < 10; i++) {
            assertEquals(1, countOccurrences[i]);
        }
    } 
}
