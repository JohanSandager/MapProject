package model;

import java.util.List;
import java.util.TreeMap;

import javafx.scene.paint.Color;

/**
 * Class responsible for creating the correct MapObject with corresponding attributes
 */
public class MapObjectBuilder {
    private TreeMap<String, MapObjectInfo> highWayTypes;
    private TreeMap<String, MapObjectInfo> areaTypes;

    public MapObjectBuilder() {
        highWayTypes = new TreeMap<>();
        SerializableColor[] footwayColors = new SerializableColor[2];
        footwayColors[0] = new SerializableColor(Color.rgb(75, 89, 110));
        footwayColors[1] = new SerializableColor(Color.rgb(255, 255, 255, 1.0));
        highWayTypes.put("footway", new MapObjectInfo(0.3, footwayColors, 1.0));
        highWayTypes.put("cycleway", highWayTypes.get("footway"));
        SerializableColor[] roadColors = new SerializableColor[2];
        roadColors[0] = new SerializableColor(Color.rgb(74, 86, 109, 1.0));
        roadColors[1] = new SerializableColor(Color.rgb(255, 255, 255, 1.0));
        highWayTypes.put("primary", new MapObjectInfo(0.5, roadColors, 1));
        highWayTypes.put("secondary", highWayTypes.get("primary"));
        highWayTypes.put("tertiary", highWayTypes.get("primary"));
        highWayTypes.put("residential", highWayTypes.get("footway")); 
        highWayTypes.put("service", highWayTypes.get("footway"));
        highWayTypes.put("path", highWayTypes.get("primary"));
        areaTypes = new TreeMap<>();
        SerializableColor[] forestColors = new SerializableColor[2];
        forestColors[0] = new SerializableColor(Color.rgb(92, 116, 87));
        forestColors[1] = new SerializableColor(Color.rgb(92, 116, 87, 1.0));
        areaTypes.put("forest", new MapObjectInfo(0.9, forestColors, 1));
        areaTypes.put("woodland", areaTypes.get("forest"));
        SerializableColor[] buildingColors = new SerializableColor[2];
        buildingColors[0] = new SerializableColor(Color.rgb(39,76,119));
        buildingColors[1] = new SerializableColor(Color.rgb(39, 76, 119, 1.0));
        areaTypes.put("building", new MapObjectInfo(0.15, buildingColors, 1));
        SerializableColor[] farmlandColors = new SerializableColor[2];
        farmlandColors[0] = new SerializableColor(Color.rgb(143, 167, 139));
        farmlandColors[1] = new SerializableColor(Color.rgb(93, 117, 89, 1.0));
        areaTypes.put("farmland", new MapObjectInfo(0.9, farmlandColors, 1));
        SerializableColor[] meadowColors = new SerializableColor[2];
        meadowColors[0] = new SerializableColor(Color.rgb(177, 194, 174));
        meadowColors[1] = new SerializableColor(Color.rgb(255, 255, 255, 1.0));
        areaTypes.put("meadow", new MapObjectInfo(0.9, meadowColors, 1));
        SerializableColor[] waterColors = new SerializableColor[2];
        waterColors[0] = new SerializableColor(Color.rgb(163, 206, 241));
        waterColors[1] = new SerializableColor(Color.rgb(255, 255, 255, 1.0));
        areaTypes.put("water", new MapObjectInfo(0.4, waterColors, 1));
        SerializableColor[] sandColors = new SerializableColor[2];
        sandColors[0] = new SerializableColor(Color.rgb(177, 194, 174));
        sandColors[1] = new SerializableColor(Color.rgb(255, 255, 255, 1.0));
        areaTypes.put("sand", new MapObjectInfo(0.9, sandColors, 1));
        SerializableColor[] grasslandColors = new SerializableColor[2];
        grasslandColors[0] = new SerializableColor(Color.rgb(199, 211, 197));
        grasslandColors[1] = new SerializableColor(Color.rgb(255, 255, 255, 1.0));
        areaTypes.put("grassland", new MapObjectInfo(0.9, grasslandColors, 1));
        SerializableColor[] cliffColors = new SerializableColor[2];
        cliffColors[0] = new SerializableColor(Color.rgb(50, 74, 95));
        cliffColors[1] = new SerializableColor(Color.rgb(255, 255, 255, 1.0));
        areaTypes.put("cliff", new MapObjectInfo(0.4, cliffColors, 1));
    }

    /**
     * The method that creates the MapObject
     * @param k the key
     * @param v the value
     * @param nodes the nodes that makes up the map object
     * @param roadName the name of the road (only relevant for highways, else just leave empty)
     * @return a map object
     */
    public MapObject createMapObject(String k, String v, List<Node> nodes, String roadName) {
        switch (k) {
            case "highway":
                Highway highWay;
                if (highWayTypes.containsKey(v)) {
                    highWay = new Highway(nodes, highWayTypes.get(v), roadName);
                } else {
                    highWay = new Highway(nodes, highWayTypes.get("residential"), roadName);
                }
                return highWay;
            case "building":
                if (v.equals("yes")) {
                    return new Area(nodes, areaTypes.get("building"));
                }
                break;
            case "place":
                break;
            case "natural":
                switch (v) {
                    case "sand":
                        return new Area(nodes, areaTypes.get("sand"));
                    case "water":
                        return new Area(nodes, areaTypes.get("water"));
                    case "grassland":
                        return new Area(nodes, areaTypes.get("grassland"));
                    case "wood":
                        return new Area(nodes, areaTypes.get("grassland"));
                }
                return new WayLine(nodes);
            case "landuse":
                switch (v) {
                    case "farmland":
                        return new Area(nodes, areaTypes.get("farmland"));
                    case "forest":
                        return new Area(nodes, areaTypes.get("forest"));
                    case "meadow":
                        return new Area(nodes, areaTypes.get("meadow"));
                }
                break;
        }
        return new Road(nodes);
    }
}
