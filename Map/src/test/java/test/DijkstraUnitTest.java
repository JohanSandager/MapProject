package test;

import model.Dijkstra;
import model.Graph;
import model.Graph.Edge;
import model.Graph.TravelPermission;
import model.Graph.roadPermissions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;


public class DijkstraUnitTest {
    private Dijkstra dijkstra; 
    private Random rnd; 
    private int testGraphSize = 10000; 
    private Graph graph; 
    private int node;

    @BeforeEach void setup() {
        graph = new Graph(); 
        rnd = new Random(); 
        double[] randomXCoords = new double[testGraphSize];  
        double[] randomYCoords = new double[testGraphSize]; 

        // generate points
        for(int i = 0; i < testGraphSize; i++) {
            randomXCoords[i] = rnd.nextDouble() * 20;
            randomYCoords[i] = rnd.nextDouble() * 20;
        }

        for(int j = 0; j < randomXCoords.length; j++) {
            graph.addNode(randomXCoords[j], randomYCoords[j]);
        }

        // generate edges 
        for(int i = 0; i < testGraphSize * 10; i++) {
            int point1 = i % testGraphSize, point2 = rnd.nextInt(i % testGraphSize + 1); 
            graph.addEdge(point1, point2, 0, rnd.nextDouble(), null, roadPermissions.all);
            graph.addEdge(point2, point1, 0, rnd.nextDouble(), null, roadPermissions.all);
        }

        node = 0; 
        int oldNode = 0; 
        // find some reachable target vertex by just jumping around at random
        for(int i = 0; i < testGraphSize * 20; i++) {
            Edge[] adjs = graph.getAdjacentNodes(node); 
            if(adjs.length == 0) { node = oldNode; i--; continue; }
            oldNode = node = adjs[rnd.nextInt(adjs.length)].toNode; 
        }
        dijkstra = new Dijkstra(graph, 0, node, 1, TravelPermission.drivable);
    }

    @Test void findRandomPath() {
        int formerNode = 0; 
        double totalDist = 0; 
        // here we basically just test that it's a coherent path
        for(Edge edge : dijkstra.getPath()) {
            totalDist += edge.distance; 
            assertEquals(formerNode, edge.fromNode);
            formerNode = edge.toNode; 
        } 
        assertTrue(totalDist <= testGraphSize*4); // the path can at most be all the edges long
    }
    
}
