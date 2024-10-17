package model;

import javafx.scene.control.Label;


public class FPSCounter {
    private static FPSCounter counter;
    private double fpsToShow;
    private double[] fpsStamps;
    private double oldTime;
    private double printTime;
    private int i;
    private Label fpsViewer;
    /**
     * Singleton that keeps track of frames per second when panning and zooming
     */
    private FPSCounter() {
        fpsToShow = 0;
        fpsStamps = new double[30];
        oldTime = System.currentTimeMillis();
        fpsViewer = new Label();
    }

    /**
     * Method for fetching the instance, while also making sure there is only one
     */
    public static FPSCounter getInstance() {
        if (counter == null) {
            counter = new FPSCounter();
        }
        return counter;
    }

    /**
     * Updates the fps counter and displays it on a label in UI
     * @param curTime Current time in milliseconds
     */
    public void updateFPS (double curTime) {
        double delta = curTime - oldTime;
        if (delta > 4) {
            double fps_stamp = Math.round(1 / (delta / 1000));
            fpsStamps[i] = (int) fps_stamp;
            if (i == 29) { i = 0; } else { i++; }
            if ((curTime - printTime) > 1000) {
                double total = 0;
                int count = 0;
                for (double stamp : fpsStamps) {
                    if (stamp < 100) {
                        total += stamp;
                        count++;
                    }
                }
                fpsToShow = Math.round(total / count);
                fpsViewer.setText(fpsToShow + "");
                printTime = curTime;
            }
        }
        oldTime = curTime;
    }

    /**
     * Choosing which label should display fps
     * @param label Label for displaying fps
     */
    public void setFPSViewer(Label label) {
        fpsViewer = label;
    }
}
