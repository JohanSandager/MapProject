package test;

import model.DataTypes.DoubleIndexMinPQ;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.util.List;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DoubleIndexMinPQTest {
    private DoubleIndexMinPQ minPQ;  
    private List<Integer> expectedOutputIndices; 
    private Double[] expectedOutputOrder;
    private Double[] testWeights;
    private static final int testSize = 64;   
    private Random rnd; 
    
    @BeforeEach void setup() { // we just initialize stuff and then determine the expected order in the naive n^2 way. 

        testWeights = new Double[testSize]; 
        rnd = new Random(); 
        for(int i = 0; i < testSize; i++) {
            testWeights[i] = rnd.nextDouble(); 
        }

        minPQ = new DoubleIndexMinPQ(testSize); 
        expectedOutputOrder = Arrays.copyOf(testWeights, testWeights.length);
        Arrays.sort(expectedOutputOrder); 
        expectedOutputIndices = new ArrayList<>(); 
        for(int i = 0; i < expectedOutputOrder.length; i++) {
            for(int k = 0; k < testWeights.length; k++) {
                if(expectedOutputOrder[i] == testWeights[k]) {
                    expectedOutputIndices.add(k);
                    break; 
                }
            }
        }
    }

    @Test void addAllThenRemoveAll() {
        
        for(int i = 0; i < testWeights.length; i++) {
            minPQ.insertOrReplace(testWeights[i], i);
        }
        List<Integer> resList = new ArrayList<>();  
        while(!minPQ.isEmpty()) {
            int in = minPQ.delMinIndex(); 
            resList.add(in); 
        }
        
        for(int i = 0; i < resList.size(); i++) {
            assertEquals(expectedOutputIndices.get(i), resList.get(i));
        }

    }   
    // cant be arsed rn, considering we need new validation code
    /* @Test void addAndRemoveInRandomOrder() {
        int addedSoFar = 0, removedSoFar = 0; 
        rnd.nextInt(testSize); 
    } */
}
