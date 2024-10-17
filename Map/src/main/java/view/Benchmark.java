package view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import model.AStar;
import model.Dijkstra;
import model.FileLoader;
import model.Graph;
import model.MapObject;
import model.RTree;
import model.SerializableRectangle2D;
import model.DataTypes.DPHT2DTree;
import model.DataTypes.DoubleArrayList;
import model.DataTypes.DoubleIndexMinPQ;
import model.DataTypes.DoublePointHashTable;
import model.DataTypes.LongToIntMap;
import model.Graph.TravelPermission;

public class Benchmark {
    public void benchmark() {
        int amtIterations = (int) Math.pow(10, 7);
        long start, end;
        // Our arrayList vs default arrayList
        long defaultTime, ourTime;
        start = System.currentTimeMillis();
        ArrayList<Double> dArrList = new ArrayList<>();
        for (int i = 0; i < amtIterations; i++) {
            dArrList.add(5.0);
        }
        for (int i = 0; i < amtIterations; i++) {
            dArrList.get(i);
        }

        end = System.currentTimeMillis();
        defaultTime = end - start;
        dArrList = null;

        start = System.currentTimeMillis();
        DoubleArrayList ourArrList = new DoubleArrayList();
        for (int i = 0; i < amtIterations; i++) {
            ourArrList.add(5.0);
        }
        for (int i = 0; i < amtIterations; i++) {
            ourArrList.get(i);
        }

        end = System.currentTimeMillis();
        ourTime = end - start;
        System.out.println("Default arraylist time: " + defaultTime + " Our arraylist time: " + ourTime
                + " and diff in favour of uur: " + (defaultTime - ourTime));
        ourArrList = null;
        // Our hashmap vs default
        start = System.currentTimeMillis();
        HashMap<Long, Integer> defHashMap = new HashMap<>();
        for (long i = 0; i < amtIterations; i++) {
            defHashMap.put(i, 69);
        }
        for (long i = 0; i < amtIterations; i++) {
            defHashMap.get(i);
        }

        end = System.currentTimeMillis();
        defaultTime = end - start;
        defHashMap = null; // deallocate for memory

        start = System.currentTimeMillis();
        LongToIntMap ourMap = new LongToIntMap();
        for (long i = 0; i < amtIterations; i++) {
            ourMap.put(i, 69);
        }
        for (long i = 0; i < amtIterations; i++) {
            ourMap.get(i);
        }

        end = System.currentTimeMillis();
        ourTime = end - start;
        System.out.println("Default hashmap time: " + defaultTime + " Our hashmap time: " + ourTime
                + " and diff in favour of our: " + (defaultTime - ourTime));
        ourMap = null;
        // Our hashtable vs default
        start = System.currentTimeMillis();
        DoublePointHashTable dpht = new DoublePointHashTable();
        for (int i = 0; i < amtIterations; i++) {
            dpht.add(i, i);
        }
        System.out.print("DoublePointHashTable: Adding time: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        for (int i = 0; i < amtIterations; i++) {
            dpht.findID(i, i);
        }
        System.out.print(" Finding by coords time: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        for (int i = 0; i < amtIterations; i++) {
            dpht.getXFromID(i);
        }
        System.out.println(" Finding by id time: " + (System.currentTimeMillis() - start));

        // Our index min pq vs default
        start = System.currentTimeMillis();
        DoubleIndexMinPQ queue = new DoubleIndexMinPQ(amtIterations);
        for (int i = 0; i < amtIterations; i++) {
            queue.insert(i - 0.5, i);
        }
        System.out.print("DoubleIndexMinPQ: Insertion time: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < amtIterations; i++) {
            queue.replace(i, i + 0.5);
        }
        System.out.print(" replacing time: " + (System.currentTimeMillis() - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < amtIterations; i++) {
            queue.delMinIndex();
        }
        System.out.println(" deleting min time: " + (System.currentTimeMillis() - start));

        // 2D tree
        start = System.currentTimeMillis();
        DPHT2DTree tree = new DPHT2DTree(dpht);
        System.out.println("DPHT2DTree construction time: " + (System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        for (int i = 0; i < amtIterations; i++) {
            tree.getClosestNodeID(i, i);
        }
        System.out.println("DPHT2DTree getting every node time: " + (System.currentTimeMillis() - start));

        // RTree
        Random rnd = new Random();
        // need to make some random
        List<MapObject> objs;
        SerializableRectangle2D bounds;
        Graph graph;
        try {
            FileLoader f = FileLoader.load("src/data/bornholm.osm");
            objs = f.getObjects();
            bounds = f.getBounds();
            graph = f.getGraph();
        } catch (Exception e) {
            throw new RuntimeException(
                    "lalalalalaalal ualalalalalala quueruerueruehrieqgrhliqewgrhljkewgrhjkewghrjlkegqwrhjlkqwegrhjklqwegrhjklqwegrhjleqghrlhleqwjreq");
        }

        start = System.currentTimeMillis();
        RTree rTree = new RTree(bounds, objs);
        System.out.println("RTree construction time w. bornholm: " + (System.currentTimeMillis() - start));

        // Djikstra vs Astar
        int startNode = 21095, endNode = 21456; // long route
        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            new Dijkstra(graph, startNode, endNode, 130, TravelPermission.drivable);
        }
        defaultTime = System.currentTimeMillis() - start;
        System.out.print("100x Djikstra time: " + defaultTime);

        start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            new AStar(graph, startNode, endNode, 130, TravelPermission.drivable);
        }
        defaultTime = System.currentTimeMillis() - start;
        System.out.println(" and 100x A* time: " + defaultTime);
    }

}
