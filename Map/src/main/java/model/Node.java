package model;

import java.util.Map;

public class Node extends OSMElement {
    public float lat, lon;

    public Node(float lat, float lon, Map<String, String> tags) {
        this.tags = tags;
        this.lat = lat;
        this.lon = lon;
    }

}
