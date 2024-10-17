package model;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import model.DataTypes.IntArrayList;
import model.DataTypes.LongArrayList;
import model.DataTypes.LongToIntMap;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.zip.ZipInputStream;

public class FileLoader extends ResourceLoader implements Loader, Serializable {

    private final String filename;
    private double minlat, maxlat, minlon, maxlon;
    private final List<MapObject> objects;
    private List<Address> addresses;
    private Graph graph;
    private final AddressRegistry addressRegistry;

    /**
     * This static function will load the given file, first checking if the file is
     * .obj.
     * If so it will quickly load the binary file, if not it will create a new
     * FileLoader object.
     * 
     * @param filePath the path of the file that is to be loaded
     */
    public static FileLoader load(String filePath)
            throws IOException, ClassNotFoundException, XMLStreamException, FactoryConfigurationError {
        if (filePath.endsWith(".obj")) {
            try (var in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(filePath)))) {
                return (FileLoader) in.readObject();
            }
        }
        return new FileLoader(filePath);
    }

    public static FileLoader loadResourceFile(String name)
            throws IOException, ClassNotFoundException, XMLStreamException, FactoryConfigurationError {
        FileLoader qwe = new FileLoader();
        if (name.endsWith(".obj")) {
            try (var in = new ObjectInputStream(new BufferedInputStream(qwe.getResourceStream(name)))) {
                return (FileLoader) in.readObject();
            }
        } else {
            throw new IllegalArgumentException("Tried to load a non .obj resource file \"" + name + "\"");
        }
    }

    private FileLoader() { // constructor so we can use resourcestreams
        this.filename = "qwe";
        this.objects = null;
        this.addresses = null;
        this.addressRegistry = null;
    }

    /**
     * The constructor for the FileLoader, meant to load any file of the extension
     * .osm or .osm.zip.
     * When finished executing, it will save a copy of the file in binary format.
     * 
     * @param filePath the path of the file that is to be loaded
     */
    public FileLoader(String filePath) throws XMLStreamException, IOException {
        this.filename = filePath;
        this.objects = new ArrayList<>();
        addresses = new ArrayList<>();
        this.addressRegistry = new AddressRegistry();
        if (filename.endsWith(".osm.zip")) {
            parseZIP();
        } else if (filename.endsWith(".osm")) {
            parseOSM(filename);
        } else {
            throw new IllegalArgumentException("The inputted file can not be parsed");
        }

    }

    @Override
    public List<MapObject> getObjects() throws IOException, XMLStreamException {
        return objects;

    }

    /**
     * Used to parse a .zip file, propagating the input to the parseOSM function.
     **/
    private void parseZIP() throws IOException, XMLStreamException {
        var input = new ZipInputStream(new FileInputStream(filename));
        input.getNextEntry();
        parseOSM(input);
    }

    /**
     * This function will create a FileInputStream and propagating it downwards,
     * when the parsing finishes it will save a binary file.
     * 
     * @param filePath the path of the file to be loaded
     */
    private void parseOSM(String filePath) throws IOException, XMLStreamException, FactoryConfigurationError {
        parseOSM(new FileInputStream(filePath));
        save(filePath + ".obj");
    }

    /**
     * The main function responsible for parsing the OSM data.
     * 
     * @param inputStream the inputStream
     */
    private void parseOSM(InputStream inputStream) throws XMLStreamException {
        InputStreamReader inputStreamReader;
        inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        System.out.println("ENCODING:" + inputStreamReader.getEncoding());
        var input = XMLInputFactory.newInstance().createXMLStreamReader(inputStreamReader);
        graph = new Graph();
        MapObjectBuilder mapObjectBuilder = new MapObjectBuilder();
        // idea here is to replace the current reference type hashmaps with the custom
        // hashmap that will map to indices in an array
        // since idToNode is a big space-eater.
        LongToIntMap idToNodeIndex = new LongToIntMap();
        LongToIntMap wayidToPermissionOrdinal = new LongToIntMap();
        LongToIntMap wayIdToSpeed = new LongToIntMap();
        LongToIntMap nodeIdToAmtHighWay = new LongToIntMap();
        LongArrayList idsInWay = new LongArrayList();
        ArrayList<LongArrayList> nodeIDofWay = new ArrayList<>();
        ArrayList<SerializablePoint2D> addressPoints = new ArrayList<>();
        IntArrayList highwayID = new IntArrayList();
        var idToWay = new HashMap<Long, Way>();
        var idToRelation = new HashMap<Long, Relation>();
        var tags = new HashMap<String, String>();
        var nodes = new ArrayList<Node>();
        ArrayList<Node> globalNodes = new ArrayList<>();
        var members = new ArrayList<OSMElement>();
        long relationId = -1;
        long wayId = -1;
        long curID = 0;
        System.out.println("STARTING OSM PARSING:");
        while (input.hasNext()) {
            var tagKind = input.next();
            if (tagKind == XMLStreamConstants.START_ELEMENT) {
                var name = input.getLocalName();
                switch (name) {
                    case "bounds":
                        minlat = Double.parseDouble(input.getAttributeValue(null, "minlat"));
                        maxlat = Double.parseDouble(input.getAttributeValue(null, "maxlat"));
                        minlon = Double.parseDouble(input.getAttributeValue(null, "minlon"));
                        maxlon = Double.parseDouble(input.getAttributeValue(null, "maxlon"));
                        break;
                    case "node":
                        long id = curID = Long.parseLong(input.getAttributeValue(null, "id"));
                        var lat = Float.parseFloat(input.getAttributeValue(null, "lat"));
                        var lon = Float.parseFloat(input.getAttributeValue(null, "lon"));
                        Node thisNode = new Node(lat, lon, tags);
                        if (!idToNodeIndex.containsKey(id)) {
                            globalNodes.add(thisNode);
                            idToNodeIndex.put(id, globalNodes.size() - 1);
                        }
                        break;
                    case "tag":
                        String key = input.getAttributeValue(null, "k").intern();
                        String value = input.getAttributeValue(null, "v").intern();
                        tags.put(key, value);

                        if (idToNodeIndex.containsKey(curID) && key.equals("osak:identifier")) {
                            int nodeID = idToNodeIndex.get(curID);
                            Node node = globalNodes.get(nodeID);
                            SerializablePoint2D point = new SerializablePoint2D(node.lon * 0.56, -node.lat);
                            String municipality = node.getTagValue("addr:municipality");
                            String postcode = node.getTagValue("addr:postcode");
                            String city = node.getTagValue("addr:city");
                            String street = node.getTagValue("addr:street");
                            String houseNumber = node.getTagValue("addr:housenumber");
                            Address newAddr = new Address(municipality, postcode, city, street, houseNumber);
                            addressPoints.add(point);
                            addresses.add(newAddr);
                        }
                        break;
                    case "nd": {
                        var reference = Long.parseLong(input.getAttributeValue(null, "ref"));

                        int nodeID = idToNodeIndex.get(reference);
                        Node node = globalNodes.get(nodeID);
                        nodes.add(node);
                        idsInWay.add(reference);
                        break;
                    }
                    case "way":
                        wayId = Long.parseLong(input.getAttributeValue(null, "id"));
                        nodes.clear();
                        idsInWay = new LongArrayList();
                        break;
                    case "relation":
                        relationId = Long.parseLong(input.getAttributeValue(null, "id"));
                        members.clear();
                        break;
                    case "member": {
                        var reference = Long.parseLong(input.getAttributeValue(null, "ref"));
                        var type = input.getAttributeValue(null, "type");
                        switch (type) {
                            case "node":
                                if (idToNodeIndex.containsKey(reference)) { // this little debug shows that, for thurø
                                                                            // at least, 26559198 and 260251585 are
                                                                            // null-keys, but we just implicitly handle
                                                                            // that
                                    int nodeID = idToNodeIndex.get(reference);
                                    members.add(globalNodes.get(nodeID));
                                }
                                break;
                            case "way":
                                members.add(idToWay.get(reference));
                                break;
                            case "relation":
                                members.add(idToRelation.get(reference));
                                break;
                        }
                        break;
                    }
                }
            } else if (tagKind == XMLStreamConstants.END_ELEMENT) {
                var elementName = input.getLocalName();

                if (elementName.equals("relation")) {
                    if (relationId != -1)
                        idToRelation.put(relationId, new Relation(members, tags));
                    Relation currentRelation = idToRelation.get(relationId);
                    String key = "";
                    String value = "";
                    if (currentRelation.getTagValue("place") != null) {
                        key = "place";
                        value = currentRelation.getTagValue(key);
                        // relationMapObject.add(mapObjectBuilder.createMapObject(key, value,
                        // currentRelation.getNodes()));
                    }
                }

                if (nodes.size() >= 2 && elementName.equals("way")) {

                    Way currentWay;
                    if (idToWay.containsKey(wayId)) {
                        System.out.println("There is some duplicate way");
                        currentWay = idToWay.get(wayId);
                    } else {
                        currentWay = new Way(nodes, tags);
                        idToWay.put(wayId, currentWay);
                    }
                    String key = "";
                    String value = "";
                    String name = "roadMcRoadson";
                    String roadType = currentWay.getTagValue("highway");
                    if (roadType != null) {
                        key = "highway";
                        value = currentWay.getTagValue(key);
                        if (currentWay.getTagValue("name") != null)
                            name = currentWay.getTagValue("name");

                        if (nodeIdToAmtHighWay.containsKey(idsInWay.get(0))) {
                            int amtHighways = nodeIdToAmtHighWay.get(idsInWay.get(0));
                            amtHighways++; // this highway
                            nodeIdToAmtHighWay.replace(idsInWay.get(0), amtHighways);
                        } else {
                            nodeIdToAmtHighWay.put(idsInWay.get(0), 1);
                        }

                        for (int i = 1; i < idsInWay.size(); i++) {
                            long prevWayID = idsInWay.get(i - 1);
                            long wayID = idsInWay.get(i);

                            if (nodeIdToAmtHighWay.containsKey(wayID)) {
                                int amtHighways = nodeIdToAmtHighWay.get(wayID);
                                amtHighways++; // this highway
                                nodeIdToAmtHighWay.replace(wayID, amtHighways);
                            } else {
                                nodeIdToAmtHighWay.put(wayID, 1);
                            }

                            int amtHighways = nodeIdToAmtHighWay.get(prevWayID);
                            amtHighways++; // this highway
                            nodeIdToAmtHighWay.replace(prevWayID, amtHighways);

                        }
                        nodeIDofWay.add(idsInWay);
                        highwayID.add(objects.size());

                        switch (roadType) {
                            case "cycleway":
                                wayidToPermissionOrdinal.put(objects.size(),
                                        Graph.roadPermissions.onlyCyclable.ordinal());
                                break;
                            case "footway":
                                wayidToPermissionOrdinal.put(objects.size(),
                                        Graph.roadPermissions.onlyWalkable.ordinal());
                                break;
                            case "motorway":
                                wayidToPermissionOrdinal.put(objects.size(),
                                        Graph.roadPermissions.onlyDrivable.ordinal());
                                break;
                            default:
                                wayidToPermissionOrdinal.put(objects.size(), Graph.roadPermissions.all.ordinal());
                                break;
                        }
                        if (currentWay.getTagValue("maxspeed") != null
                                && currentWay.getTagValue("maxspeed").matches("\\d+")) {
                            wayIdToSpeed.put(objects.size(), Integer.parseInt(currentWay.getTagValue("maxspeed")));
                        } else {
                            wayIdToSpeed.put(objects.size(), 50);
                        }

                    } else if (currentWay.getTagValue("building") != null) {
                        key = "building";
                        value = currentWay.getTagValue(key);
                    } else if (currentWay.getTagValue("natural") != null) {
                        key = "natural";
                        value = currentWay.getTagValue(key);
                    } else if (currentWay.getTagValue("landuse") != null) {
                        key = "landuse";
                        value = currentWay.getTagValue(key);
                    }
                    MapObject object = mapObjectBuilder.createMapObject(key, value, nodes, name);
                    if (object != null) {
                        objects.add(object);
                    }

                }

                if (elementName.equals("node") || elementName.equals("relation") || elementName.equals("way")) {
                    tags.clear();
                }
            }
        }

        System.out.println("OSM PARSING DONE");

        LongToIntMap nodeIdToGraphID = new LongToIntMap();

        System.out.println("COLLAPSING NODES w. " + nodeIdToAmtHighWay.size() + " nodes ");
        for (Long id : nodeIdToAmtHighWay) { // loop over the keys that are
            if (nodeIdToAmtHighWay.get(id) == 2) {
                nodeIdToAmtHighWay.remove(id);
                continue;
            }
            Node node = globalNodes.get(idToNodeIndex.get(id));
            double x = node.lon * 0.56;
            double y = node.lat * -1;
            nodeIdToGraphID.put(id, graph.addNode(x, y));
        }

        nodeIdToAmtHighWay = null;
        System.out
                .println("COLLAPSING NODES DONE\nSTARTING GRAPH CONSTRUCTION w. " + highwayID.size() + " ways/ edges");
        for (int i = 0; i < highwayID.size(); i++) {
            LongArrayList way = nodeIDofWay.get(i);
            Highway highway = (Highway) objects.get(highwayID.get(i));
            int prevNodeId;
            int permissionOrdinal = wayidToPermissionOrdinal.get(highwayID.get(i));
            int speed = wayIdToSpeed.get(highwayID.get(i));

            // set prevNodeId to the first node, and ensure that it exists
            if (nodeIdToGraphID.containsKey(way.get(0))) {
                prevNodeId = nodeIdToGraphID.get(way.get(0));
            } else {
                Node node = globalNodes.get(idToNodeIndex.get(way.get(0)));
                double x = node.lon * 0.56;
                double y = node.lat * -1;
                prevNodeId = graph.addNode(x, y);
                nodeIdToGraphID.put(way.get(0), prevNodeId); // we always need to have the starting nodes of a graph
            }

            // and ensure that the last node also exists, but obviously we don't need to
            // remember it
            if (!nodeIdToGraphID.containsKey(way.get(way.size() - 1))) {
                Node node = globalNodes.get(idToNodeIndex.get(way.get(way.size() - 1)));
                double x = node.lon * 0.56;
                double y = node.lat * -1;
                nodeIdToGraphID.put(way.get(way.size() - 1), graph.addNode(x, y)); // we always need to have the
                                                                                   // starting nodes of a graph
            }
            double distToAdd = 0;
            for (int j = 1; j < way.size(); j++) {

                if (nodeIdToGraphID.containsKey(way.get(j))) {
                    int curNodeId = nodeIdToGraphID.get(way.get(j));
                    graph.addEdge(prevNodeId, curNodeId, distToAdd, speed, highway,
                            Graph.roadPermissions.values()[permissionOrdinal]);
                    graph.addEdge(curNodeId, prevNodeId, distToAdd, speed, highway,
                            Graph.roadPermissions.values()[permissionOrdinal]);
                    distToAdd = 0;
                    prevNodeId = curNodeId;
                    continue;
                }

                Node prevNode = globalNodes.get(idToNodeIndex.get(way.get(j)));
                double prevX = prevNode.lon * 0.56;
                double prevY = prevNode.lat * -1;
                Node node = globalNodes.get(idToNodeIndex.get(way.get(j - 1)));
                double x = node.lon * 0.56;
                double y = node.lat * -1;
                distToAdd += Graph.getDistanceInMetersFromCoordinates(x, y, prevX, prevY);
            }
        }

        // deallocate everything explicitly, since the FileLoader will live on to
        // provide via its getter functions.
        globalNodes = null;
        nodeIdToGraphID = null;
        nodeIDofWay = null;
        idToRelation = null;
        idToNodeIndex = null;
        System.out.println(
                "GRAPH CONSTRUCTION DONE\nSTARTING ADDRESS CONSTRUCTION w. " + addresses.size() + " addresses ");
        for (int i = 0; i < addresses.size(); i++) {
            int graphPoint = graph.getSmallestDistanceNodeID(addressPoints.get(i).getX(), addressPoints.get(i).getY());
            addresses.get(i).setClosestGraphPoint(graphPoint);
            addressRegistry.addAddress(addresses.get(i));
        }
        graph.rmBestDistTree();
        addresses = null;
        System.out.println("ADDRESS CONSTRUCTION DONE, " + objects.size() + " MapObjects created");
    }

    /**
     * Saves the object as a binary file
     * 
     * @param filePath the path of the file to witch the .obj should be added
     */
    private void save(String filePath) throws IOException {
        try (var out = new ObjectOutputStream(new FileOutputStream(filePath))) {
            out.writeObject(this);
        }
    }

    /*
     * private void writeObject(java.io.ObjectOutputStream out) throws IOException {
     * System.out.println("Writing objects to binary");
     * out.writeObject(objects);
     * System.out.println("Writing graph to binary");
     * out.writeObject(graph);
     * System.out.println("Writing adressRegistry to binary");
     * out.writeObject(addressRegistry);
     * }
     */

    @Override
    public boolean isLoadable() {
        return filename.endsWith(".osm.zip") || filename.endsWith(".osm");
    }

    /**
     * Returns the AddressRegistry
     **/
    public AddressRegistry getAddressRegistry() {
        return addressRegistry;
    }

    /**
     * Returns the graph
     */
    public Graph getGraph() {
        return graph;
    }

    /**
     * Main method, mainly for debugging purposes
     */
    public static void main(String[] args) {
        long start, end;
        try {
            start = System.currentTimeMillis();
            FileLoader fl = new FileLoader("somePath");
            System.out.println(fl.addresses.size());
            end = System.currentTimeMillis();
            double time = (end - start) / 1000.0;
            System.out.println("Loaded: " + fl.objects.size() + " objects in " + time + " seconds");
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * For testing average load time, chiefly for debug purposes
     */
    public static void testLoadTime() {
        List<Double> runtimes = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            long start, end;
            try {
                start = System.currentTimeMillis();
                FileLoader fl = new FileLoader("./src/data/thuro.osm");
                fl.getObjects();
                end = System.currentTimeMillis();
                double time = (end - start) / 1000.0;
                runtimes.add(time);
            } catch (IOException | XMLStreamException e) {
                throw new RuntimeException(e);
            }
        }
        double sum = 0.0;
        for (double runtime : runtimes) {
            sum += runtime;
        }
        double average = sum / (runtimes.size() * 1.0);
        System.out.println("Loaded " + runtimes.size() + " in on average " + average + " seconds");
    }

    /**
     * Returns the bounds for the entire file
     */
    public SerializableRectangle2D getBounds() {
        return new SerializableRectangle2D(0.56 * minlon, -minlat, 0.56 * (maxlon - minlon), Math.abs(maxlat - minlat));
    }
}
