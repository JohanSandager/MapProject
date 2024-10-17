package model;

import java.util.*;

public class Relation extends OSMElement {
    private OSMElement[] members;
    private String type;

    public Relation(List<OSMElement> members, Map<String, String> tags) {
        this.members = copyValues(members);
        this.tags = tags;
        type = tags.get("type");
    }

    public String getType() {
        return type;
    }

    private OSMElement[] copyValues(List<OSMElement> OSMElements) {
        OSMElement[] tempMembersArray = new OSMElement[OSMElements.size()];
        for(int i = 0; i < OSMElements.size(); i++) {
            tempMembersArray[i] = OSMElements.get(i);
        }
        return tempMembersArray;
    }

    /*public List<Node> getNodes() {
        List<Node> localNodes = new ArrayList<>(); //The collective list of nodes to draw
        Node lastEndNode = null; //The pointer for the last end node
        for (MapElement element : members) { //Looping through the list of members
            if (element instanceof Way) {
                Node[] nodes = ((Way) element).getNodes(); //Getting the array of nodes
                localNodes.addAll(Arrays.asList(nodes));
            } else if (element instanceof Node) {
                Node node = (Node) element;
                localNodes.add(node);
            } else if (element instanceof Relation) {
                List<Node> nodes = ((Relation) element).getNodes();
                localNodes.addAll(nodes);
            }
        }
        return localNodes;
    }*/

    public List<Node> getNodes() {
        List<Node> localNodes = new ArrayList<>(); //The collective list of nodes to draw
        Node lastEndNode = null; //The pointer for the last end node
        for (OSMElement element : members) { //Looping through the list of members
            if (element instanceof Way) {
                Node[] nodes = ((Way) element).getNodes(); //Getting the array of nodes
                if(nodes[0] != lastEndNode && lastEndNode != null) { //If the last end node is not the first node of the array, reverse it
                    for(int i = nodes.length - 1; i >= 0; i--) {
                        localNodes.add(nodes[i]);
                    }
                    lastEndNode = nodes[0];
                } else { //else just add nodes normally
                    lastEndNode = nodes[nodes.length - 1];
                    localNodes.addAll(Arrays.asList(nodes));
                }
            } else if (element instanceof Node) {
                Node node = (Node) element;
                lastEndNode = node;
                localNodes.add(node);
            } else if (element instanceof Relation) {
                List<Node> nodes = ((Relation) element).getNodes();
                if(nodes.get(0) != lastEndNode && lastEndNode != null) {
                    for(int i = nodes.size() - 1; i >= 0; i--) {
                        localNodes.add(nodes.get(i));
                    }
                } else {
                    lastEndNode = nodes.get(nodes.size() - 1);
                    localNodes.addAll(nodes);
                }
            }
        }
        return localNodes;
    }

}

