package model;

import java.util.Map;

/**
 * Represents an element from OSM such as Node, Way or Relation
 */
public abstract class OSMElement {
    protected Map<String, String> tags;

    public String getTagValue(String key) {
        return tags.get(key);
    }

}
