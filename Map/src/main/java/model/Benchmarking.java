package model;

public class Benchmarking {

    private static long startTime;

    public static void startTimer() {
        startTime = System.currentTimeMillis();
    }

    public static double endTime() {
        long endTime = System.currentTimeMillis();
        if(startTime == 0)  return -1;
        double time = (endTime - startTime) / 1000.0;
        System.out.println("Measured: " + time + "s");
        return time;
    }

}
