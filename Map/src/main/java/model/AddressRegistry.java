package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import model.DataTypes.IntArrayList;
import model.DataTypes.IntToIntHashMap;
import model.GlobalConfig.Options;

/**
 * A class who supports the fast creation of full address objects from partial
 * address objects.
 * By partial address objects we mean e.g. just a city, or a street.
 * It achieves this by filling in possible matching elements, both upwards and
 * downwards in the hierarchy, which goes
 * Municipality > Postcodes > Cities > Streets > HouseNumbers.
 * <P>
 * So for example, if you give it an input address which only has the street
 * field filled out, in this case "Street69".
 * It would see that "Street69" has a parent, "City14", which has a parent
 * postcode "4200", which has a parent municipality "Munch123".
 * "Street69" also has a number of children, who are housenumbers. The full
 * adress then is given as "Street69, City14 4200, Munch123".
 * <P>
 * It also easily handles the case that there are multiple "Street69"s in
 * different cities, running up the chain for all of them.
 * To speed up the process, however, we at most find 512 possible addresses. Any
 * additional are ignored. Also, we only treat housenumbers
 * as passive children, not allowing queries for say, every nr. 5 in the
 * country, which would both be stupidly slow and completely meaningless for the
 * user.
 * <P>
 * One can naturally observe that there are more duplicates the lower you go,
 * with no duplicate postcodes or municipalities, and tons of duplicate streets.
 * However, the amount of duplicate housenumbers is just ridiculous, and
 * querying just a housenumber makes no sense, it's always gonna be a qualifier
 * for some
 * higher level query, which lets us leave it out.
 * <P>
 * This is achieved via a kind of domain specific tree, with nodes having one
 * parent and many children. The "layers" of the trees corrospond to the
 * semantic elements
 * of an address, Municipalities, Cities ect
 */
public class AddressRegistry implements Serializable {

    private enum AddrElements {
        MUNICIPALITY,
        POSTCODE,
        CITY,
        STREET,
        HOUSENUMBER
    }

    private ArrayList<ElementNode> eNodes;
    private TrieNode[] root;
    private ArrayList<ArrayList<TrieNode>> semanticTree;
    private IntToIntHashMap coordinates;

    private class TrieNode implements Serializable {
        public char val;
        public int parentID, lChildID, rChildID, midChildID;
        public int nodeID, thisID;

        public TrieNode(char val, int thisID) {
            this.val = val;
            this.thisID = thisID;
            this.parentID = -1;
            this.lChildID = -1;
            this.rChildID = -1;
            this.midChildID = -1;
            this.nodeID = -1;
        }

        public TrieNode(char val, int thisID, int parentID) {
            this(val, thisID);
            this.parentID = parentID;
        }

        public String getString(ArrayList<TrieNode> nodeList) {
            TrieNode curNode = this;
            TrieNode former;
            StringBuilder str = new StringBuilder();
            str.append(val);
            while (curNode != null) {
                former = curNode;

                if (curNode.parentID == -1) {
                    break;
                }
                curNode = nodeList.get(curNode.parentID);
                if (curNode.midChildID == former.thisID) {
                    str.append(curNode.val);
                }
            }
            return str.reverse().toString();
        }

    }

    private class ElementNode implements Serializable {
        public int valID;
        public AddrElements semantic;
        public int parentID;
        public int thisID;
        public int nextSiblingID;
        public IntToIntHashMap siblings;
        public IntArrayList childrenIDs;

        private ElementNode(AddrElements semantic, int valID, int thisID, boolean q) {
            this.semantic = semantic;
            this.valID = valID;
            this.childrenIDs = new IntArrayList();
            this.parentID = -1;
            this.nextSiblingID = -1;
            this.thisID = thisID;
        }

        public ElementNode(AddrElements semantic, int valID, int thisID) {
            this(semantic, valID, thisID, false);
            this.siblings = new IntToIntHashMap();
        }

        /**
         * Original node constructor, e.g the first sibling. Additional siblings can be
         * chained to this one
         * 
         * @param semantic
         * @param valID
         * @param thisID
         * @param parent
         */
        public ElementNode(AddrElements semantic, int valID, int thisID, ElementNode parent) {
            this(semantic, valID, thisID);
            if (parent == null)
                return;
            this.parentID = parent.thisID;
            siblings.put(this.parentID, thisID);
            parent.childrenIDs.add(thisID);
        }

        /**
         * Constructor for a sibling
         * 
         * @param semantic
         * @param valID
         * @param thisID
         * @param parent
         * @param siblings
         */
        private ElementNode(AddrElements semantic, int valID, int thisID, ElementNode parent,
                IntToIntHashMap siblings) {
            this(semantic, valID, thisID, false);
            this.siblings = siblings;
            if (parent != null) {
                this.parentID = parent.thisID;
                siblings.put(parentID, thisID);
                parent.childrenIDs.add(thisID);
            }
        }

        public int getOrAddSibling(ArrayList<ElementNode> nodes, ElementNode parentNode) {
            if (parentNode != null) {
                int pID = parentNode.thisID;
                if (siblings.containsKey(pID)) {
                    return siblings.get(pID);
                } // we already created such a sibling

                // we make a new sibling, which points to the same map of siblings, and maps the
                // parentID to the new sibling
                ElementNode newNode = new ElementNode(semantic, valID, nodes.size(), parentNode, siblings);
                nodes.add(newNode);
                return nodes.size() - 1;
            }
            return thisID; // if we have no valid parent, there can be no siblings
        }
    }

    public AddressRegistry() {
        root = new TrieNode[AddrElements.values().length];
        eNodes = new ArrayList<>();
        semanticTree = new ArrayList<>(AddrElements.values().length);
        coordinates = new IntToIntHashMap();
        for (int i = 0; i < AddrElements.values().length; i++) {
            semanticTree.add(new ArrayList<>());
            root[i] = new TrieNode('-', 0);
            semanticTree.get(i).add(root[i]);
        }
    }

    public void addAddress(Address addr) {
        TrieNode muncTNode = insertString(AddrElements.MUNICIPALITY, addr.getMunicipality().trim().toUpperCase());
        ElementNode municENode;
        if (muncTNode.nodeID != -1) {
            municENode = eNodes.get(muncTNode.nodeID);
        } else {
            municENode = new ElementNode(AddrElements.MUNICIPALITY, muncTNode.thisID, eNodes.size());
            muncTNode.nodeID = eNodes.size();
            eNodes.add(municENode);
            // System.out.println("Created new municipality: " + getStr(municENode));
        }

        ElementNode pcEnode = setAddrElem(municENode, AddrElements.POSTCODE, Integer.toString(addr.getPostCode()));
        ElementNode cityENode = setAddrElem(pcEnode, AddrElements.CITY, addr.getCity().trim().toUpperCase());
        ElementNode streetENode = setAddrElem(cityENode, AddrElements.STREET, addr.getStreet().trim().toUpperCase());
        ElementNode hnENode = setAddrElem(streetENode, AddrElements.HOUSENUMBER,
                addr.getHouseNumber().trim().toUpperCase());

        coordinates.put(hnENode.thisID, addr.getGraphNode());
    }

    private ElementNode setAddrElem(ElementNode parentENode, AddrElements elem, String strElem) {
        TrieNode tNode = insertString(elem, strElem);
        if (tNode.nodeID != -1) {
            int qwe = eNodes.get(tNode.nodeID).getOrAddSibling(eNodes, parentENode);
            parentENode = eNodes.get(qwe);
        } else {
            parentENode = new ElementNode(elem, tNode.thisID, eNodes.size(), parentENode);
            tNode.nodeID = eNodes.size();
            eNodes.add(parentENode);
        }
        return parentENode;
    }

    private TrieNode insertString(AddrElements elem, String str) {
        ArrayList<TrieNode> trieArr = semanticTree.get(elem.ordinal());
        TrieNode trieRoot = root[elem.ordinal()];
        TrieNode curNode = trieRoot;
        int curNodeID = 0;

        int index = 0;
        while (true) {
            if (curNodeID < 0) { // insert the rest
                if (curNodeID != curNode.midChildID) {
                    curNode = insertChild(str.charAt(index++), curNode, trieArr);
                }
                str = str.substring(index);
                for (char c : str.toCharArray()) {
                    curNode.midChildID = trieArr.size();
                    TrieNode newNode = new TrieNode(c, trieArr.size(), curNode.thisID);
                    trieArr.add(newNode);
                    curNode = newNode;
                }
                return curNode;
            }
            if (str.charAt(index) < curNode.val) {
                curNodeID = curNode.lChildID;
            } else if (str.charAt(index) > curNode.val) {
                curNodeID = curNode.rChildID;
            } else if (index < str.length() - 1) {
                curNodeID = curNode.midChildID;
                index++;
            } else {
                return curNode;
            } // the full string is already in the trie
            if (curNodeID >= 0) {
                curNode = trieArr.get(curNodeID);
            }
        }
    }

    private TrieNode find(AddrElements elem, String str) {
        return find(elem, str, root[elem.ordinal()], 0);
    }

    private TrieNode find(AddrElements elem, String str, TrieNode startNode, int strIndex) {
        str = str.trim().toUpperCase();
        ArrayList<TrieNode> trieArr = semanticTree.get(elem.ordinal());
        TrieNode curNode = startNode;
        int curNodeID = startNode.thisID; // since we start at root
        int index = strIndex;

        while (true) {
            if (str.charAt(index) < curNode.val) {
                curNodeID = curNode.lChildID;
            } else if (str.charAt(index) > curNode.val) {
                curNodeID = curNode.rChildID;
            } else if (index < str.length() - 1) {
                curNodeID = curNode.midChildID;
                index++;
            } else {
                return curNode;
            } // the full string is already in the trie
            if (curNodeID >= 0) {
                curNode = trieArr.get(curNodeID);
            } else {
                return null;
            } // string isn't in the tree
        }
    }

    private TrieNode insertChild(char childVal, TrieNode parent, ArrayList<TrieNode> trieArr) {
        TrieNode newNode;
        if (childVal < parent.val) {
            parent.lChildID = trieArr.size();
        } else if (childVal > parent.val) {
            parent.rChildID = trieArr.size();
        } else {
            parent.midChildID = trieArr.size();
        }

        newNode = new TrieNode(childVal, trieArr.size(), parent.thisID);
        trieArr.add(newNode);
        return newNode;
    }

    private Address createQueryAddress(String query) {
        query = query.trim().toUpperCase();
        String[] words = query.split(" ");
        boolean[] isRecognized = new boolean[words.length];
        Arrays.fill(isRecognized, false);

        String[] elements = new String[AddrElements.values().length];

        for (int i = 0; i < AddrElements.values().length; i++) {
            ArrayList<TrieNode> tries = semanticTree.get(i);
            int strIndex = 0;
            TrieNode node = root[i], formerNode;
            for (int k = 0; k < words.length; k++) {
                if (isRecognized[k]) {
                    continue;
                }
                if (node.thisID != root[i].thisID) {
                    words[k] = " " + words[k];
                } // because of multiple word names, names never start with a space but will have
                  // em inbetween words

                formerNode = node;
                node = find(AddrElements.values()[i], words[k], node, 0);
                if (node == null) { // this isnt a match
                    node = formerNode;
                    if (formerNode.thisID != root[i].thisID) {
                        break;
                    } // we found something valid, now gtfo
                } else {
                    isRecognized[k] = true;
                }
            }
            if (node != null && node.thisID != root[i].thisID) { // if we found something
                node = traverseDown(node, tries);
                elements[i] = node.getString(tries);
            }
        }

        return new Address(elements[AddrElements.MUNICIPALITY.ordinal()],
                elements[AddrElements.POSTCODE.ordinal()],
                elements[AddrElements.CITY.ordinal()],
                elements[AddrElements.STREET.ordinal()],
                elements[AddrElements.HOUSENUMBER.ordinal()]);
    }

    public List<Address> findAddresses(String query) {
        return findAddresses(createQueryAddress(query));
    }

    // Returns all the valid addresses that match the query address
    // query address only needs to have one non-null field.
    public List<Address> findAddresses(Address queryAddr) {
        List<Address> possibleAddresses = new ArrayList<>();
        List<ElementNode> streets = null, cities = null, postCodes = null, municipalities = null;

        if (queryAddr.getStreet() != null) {
            DebugConsole.getInstance().logIfOption(Options.ADRESS_DEBUG_INFO,
                    "Querying street: " + queryAddr.getStreet());
            TrieNode street = find(AddrElements.STREET, queryAddr.getStreet());
            streets = new LinkedList<>();
            cities = new LinkedList<>();
            postCodes = new LinkedList<>();
            municipalities = new LinkedList<>();
            ElementNode firstStreetE = eNodes.get(street.nodeID);
            streets.add(firstStreetE);

            while (firstStreetE.nextSiblingID != -1) {
                firstStreetE = eNodes.get(firstStreetE.nextSiblingID);
                streets.add(firstStreetE);
            }

            for (ElementNode s : streets) {
                cities.add(eNodes.get(s.parentID));
            }

            for (ElementNode c : cities) {
                postCodes.add(eNodes.get(c.parentID));
            }

            for (ElementNode pc : postCodes) {
                municipalities.add(eNodes.get(pc.parentID));
            }
        } else if (queryAddr.getCity() != null) { // for each level, we know there is no query for the levels below it.
                                                  // E.g here we know the user didnt specify a street
            DebugConsole.getInstance().logIfOption(Options.ADRESS_DEBUG_INFO, "Querying city: " + queryAddr.getCity());
            TrieNode city = find(AddrElements.CITY, queryAddr.getCity());
            cities = new LinkedList<>();
            postCodes = new LinkedList<>();
            municipalities = new LinkedList<>();

            ElementNode firstCityE = eNodes.get(city.nodeID);
            cities.add(firstCityE);

            while (firstCityE.nextSiblingID != -1) {
                firstCityE = eNodes.get(firstCityE.nextSiblingID);
                cities.add(firstCityE);
            }

            for (ElementNode c : cities) {
                postCodes.add(eNodes.get(c.parentID));
            }

            for (ElementNode pc : postCodes) {
                municipalities.add(eNodes.get(pc.parentID));
            }
        } else if (queryAddr.getPostCode() != -1) { // otherwise start it at the postcode level, if we have a postcode
            DebugConsole.getInstance().logIfOption(Options.ADRESS_DEBUG_INFO,
                    "Querying postcode: " + queryAddr.getPostCode());
            TrieNode postCode = find(AddrElements.POSTCODE, Integer.toString(queryAddr.getPostCode()));
            postCodes = new LinkedList<>();
            municipalities = new LinkedList<>();

            ElementNode firstPCE = eNodes.get(postCode.nodeID);
            postCodes.add(firstPCE);

            while (firstPCE.nextSiblingID != -1) {
                firstPCE = eNodes.get(firstPCE.nextSiblingID);
                postCodes.add(firstPCE);
            }

            for (ElementNode pc : postCodes) {
                municipalities.add(eNodes.get(pc.parentID));
            }

        } else if (queryAddr.getMunicipality() != null) { // if we have a municipality, start the search at top down
                                                          // level
            DebugConsole.getInstance().logIfOption(Options.ADRESS_DEBUG_INFO,
                    "Querying municipality: " + queryAddr.getMunicipality());
            TrieNode municipality = find(AddrElements.MUNICIPALITY, queryAddr.getMunicipality());
            municipalities = new LinkedList<>();

            ElementNode firstMunc = eNodes.get(municipality.nodeID);
            municipalities.add(firstMunc);

            while (firstMunc.nextSiblingID != -1) {
                firstMunc = eNodes.get(firstMunc.nextSiblingID);
                municipalities.add(firstMunc);
            }

        } else { // haram query
            return possibleAddresses;
        }

        // Here we just go down the tree, grabbing all the children of a particular
        // instance if we dont have any info on that level from the query
        // and otherwise filtering it by the ones that match the query.
        // IF the query is nonsense, e.g a street tag that's not in the given city tag
        // or something, which can easily happen with user input, we'll
        // just return an empty list, which should be fine for our case. The general
        // logic anyway is that we display one message when there is zero matches,
        // another with one match, and finally some other when theres more than one.
        // Maybe we need to filter a little bit considering that we can easily return
        // several hundred addresses if you search for some common streetname, e.g
        // "Østergade". Or just a municipality or city, in which case you get everything
        // thats inside of that particular concept, because there's no filter on
        // streetnames, so you just get em all.
        for (ElementNode munc : municipalities) {
            DebugConsole.getInstance().logIfOption(Options.ADRESS_DEBUG_INFO, munc.thisID + ":");

            List<ElementNode> postCodeList = getAppropiateList(
                    queryAddr.getPostCode() == -1 ? null : Integer.toString(queryAddr.getPostCode()), munc,
                    AddrElements.POSTCODE, postCodes);

            for (ElementNode postCode : postCodeList) {
                List<ElementNode> cityList = getAppropiateList(queryAddr.getCity(), postCode, AddrElements.CITY,
                        cities);

                for (ElementNode city : cityList) {
                    List<ElementNode> streetList = getAppropiateList(queryAddr.getStreet(), city, AddrElements.STREET,
                            streets);

                    for (ElementNode street : streetList) {
                        ElementNode houseNum;
                        if (queryAddr.getHouseNumber() == null) {
                            houseNum = eNodes.get(street.childrenIDs.get(0));
                        } else {
                            List<ElementNode> houseNums = getAppropiateList(queryAddr.getHouseNumber(), street,
                                    AddrElements.HOUSENUMBER, null);
                            houseNum = houseNums.get(0);
                        }

                        Address resAddress = new Address(getStr(munc),
                                getStr(postCode),
                                getStr(city),
                                getStr(street),
                                getStr(houseNum));
                        resAddress.setClosestGraphPoint(coordinates.get(houseNum.thisID));

                        DebugConsole.getInstance().logIfOption(Options.ADRESS_DEBUG_INFO, "{" + resAddress + "}");
                        possibleAddresses.add(resAddress);
                    }
                }
            }
        }
        DebugConsole.getInstance().logIfOption(Options.ADRESS_DEBUG_INFO,
                "Final # of possible addresses: " + possibleAddresses.size());
        return possibleAddresses;
    }

    /**
     * Utility function to get the string that a particular ElementNode represents.
     * 
     * @param eNode
     * @return
     */
    private String getStr(ElementNode eNode) {
        ArrayList<TrieNode> tArr = semanticTree.get(eNode.semantic.ordinal());

        return tArr.get(eNode.valID).getString(tArr);
    }

    /**
     * Utility function to get to the bottom of a particular word. Used to complete
     * incomplete input
     * 
     * @param origin the node to start at
     * @param tree   the particular semantic tree the node is a part of
     * @return some end-node that the origin node leads to.
     */
    private TrieNode traverseDown(TrieNode origin, ArrayList<TrieNode> tree) {
        TrieNode next = origin;
        while (next.midChildID != -1) {
            next = tree.get(next.midChildID);
        }
        return next;
    }

    private List<ElementNode> getAppropiateList(String queryStr, ElementNode parentNode, AddrElements elem,
            List<ElementNode> premadeList) {
        List<ElementNode> elemList;
        // basically we want to avoid recalculating stuff unless we have to
        if (queryStr == null) { // if we dont have a query we want to use postCodeNodes, since they denote all
                                // the valid postcodes found by upwards traversal
            if (premadeList == null) { // if we dont have access to postCodeNodes, just grab an arbitrary amount of
                                       // postcodes
                elemList = new LinkedList<>();
                for (int i = 0; i < Math.min(8, parentNode.childrenIDs.size()); i++) {
                    elemList.add(eNodes.get(parentNode.childrenIDs.get(i)));
                }
                // the worse case amount of addresses is the constant^3, or 512 at cur value.
                // MUCH better than the previous case
            } else {
                elemList = premadeList.subList(0, Math.min(8, premadeList.size()));
            }
        } else { // if we have a query, we have to do downwards traversal since we dont know if
                 // the different levels of the search match eachother
            queryStr = queryStr.trim().toUpperCase();
            elemList = new LinkedList<>();
            ArrayList<TrieNode> elemTree = semanticTree.get(elem.ordinal());
            for (int i = 0; i < parentNode.childrenIDs.size(); i++) {
                ElementNode child = eNodes.get(parentNode.childrenIDs.get(i));
                String elementStr = elemTree.get(child.valID).getString(elemTree);
                if (queryStr.equals(elementStr)) {
                    elemList.add(child);
                }

            }
        }

        return elemList;
    }

}
