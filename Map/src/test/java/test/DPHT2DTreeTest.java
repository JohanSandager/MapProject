package test;

import model.DataTypes.DPHT2DTree;
import model.DataTypes.DoublePointHashTable;
import model.SerializablePoint2D;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DPHT2DTreeTest {
    private DPHT2DTree tree; 
    private DoublePointHashTable dpht; 
    private List<Integer> minDistIDs; 
    private List<SerializablePoint2D> testPoints; 
    private List<Double> distances; 
    private static final int sampleSize = 2048; 

    @BeforeEach void setup() {
        dpht = new DoublePointHashTable(sampleSize * 2); 
        minDistIDs = new ArrayList<>(); 
        Random rnd = new Random(); 
        testPoints  = new ArrayList<>(); 
        distances = new ArrayList<>(); 
        // generate a random dataset 
        for(int i = 0; i < sampleSize; i++) {
            dpht.add(rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5); // -0.5 ensures we can correctly handle negative values as well 
        }

        // generate our validation dataset
        for(int i = 0; i < 32; i++) {
            SerializablePoint2D testPoint = new SerializablePoint2D(rnd.nextDouble() - 0.5, rnd.nextDouble() - 0.5); 
            testPoints.add(testPoint); 

            // caveman method find the lowest distance
            double minDist = Double.POSITIVE_INFINITY;
            int minDistID = -1;  
            for(int k = 0; k < dpht.size(); k++) {
                double dist = Math.pow(dpht.getXFromID(k) - testPoint.getX(),2) + Math.pow(dpht.getYFromID(k) - testPoint.getY(),2); 
                if(dist < minDist) { minDist = dist; minDistID = k;}
            }
            distances.add(minDist);
            minDistIDs.add(minDistID); 
        }
    }

    @Test void RandomPointsTest() {
        tree = new DPHT2DTree(dpht); 
        int totalAmtNodesExamined = 0; 
        for(int i = 0; i < minDistIDs.size(); i++) {
            assertEquals(minDistIDs.get(i), tree.getClosestNodeID(testPoints.get(i).getX(), testPoints.get(i).getY()));
            totalAmtNodesExamined += tree.getNodesExamined(); 
            System.out.println("Got result in " + tree.getNodesExamined() + " nodes examined"); 
        }
        System.out.println("Average nodes examined: " + (totalAmtNodesExamined * 1.0 / minDistIDs.size()) + " compared to log2(" + sampleSize + ") = " + Math.log(sampleSize) / Math.log(2));
    }
}
