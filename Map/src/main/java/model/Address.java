package model;

import java.io.Serializable;

/**
 * A class which represent a particular danish address. Generally very domain
 * specific, in that it assumes that
 * there is a hierarchy of Municipality > postcode > city > street >
 * housenumber. Which is true for all danish addresses
 * but the same cannot be said to be the case elsewhere in the world.
 * This highly domain specificity allows us to do very fast searches.
 */
public class Address implements Comparable<Address>, Serializable {
    private String municipality, city, street, houseNumber;
    private int postCode;
    private int closestGraphPoint;

    /**
     * Constructor for creating an Address on properly formatted input
     * 
     * @param municipalityTag the municipality
     * @param postCodeTag     the postcode
     * @param cityTag         the city
     * @param streetTag       the street
     * @param houseNumberTag  the house number, note it's a string, allowing for
     *                        t.h. and so on
     */
    public Address(String municipalityTag, String postCodeTag,
            String cityTag, String streetTag, String houseNumberTag) {
        this.closestGraphPoint = -1;
        this.municipality = municipalityTag;
        this.postCode = postCodeTag == null ? -1 : Integer.parseInt(postCodeTag);
        this.city = cityTag;
        this.street = streetTag;
        this.houseNumber = houseNumberTag;
    }

    /**
     * Sets the node in the graph that is closest to the address
     * 
     * @param closestNodeId the closest node
     */
    public void setClosestGraphPoint(int closestNodeId) {
        closestGraphPoint = closestNodeId;
    }

    /**
     * @return the municipality
     */
    public String getMunicipality() {
        return municipality;
    }

    /**
     * @return the city
     */
    public String getCity() {
        return city;
    }

    /**
     * @return the street
     */
    public String getStreet() {
        return street;
    }

    /**
     * Sets the municipality of the address to the specified municipality
     * 
     * @param municipality the new municiplaity
     */
    public void setMunicipality(String municipality) {
        this.municipality = municipality;
    }

    /**
     * Sets the city of the address to the specified city
     * 
     * @param city the new city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Sets the street of the address to the specified street
     * 
     * @param street the new street
     */
    public void setStreet(String street) {
        this.street = street;
    }

    /**
     * Sets the postcode of the address to the specified postcode
     * 
     * @return the new postcode
     */
    public int getPostCode() {
        return postCode;
    }

    /**
     * @return the house number
     */
    public String getHouseNumber() {
        return houseNumber;
    }

    /**
     * @return the node in the graph that is nearest to the address
     */
    public int getGraphNode() {
        return closestGraphPoint;
    }

    @Override
    public int compareTo(Address o) {
        if (!this.getMunicipality().equals(o.getMunicipality())) {
            return this.getMunicipality().compareTo(o.getMunicipality());
        } else if (!this.getCity().equals(o.getCity())) {
            return this.getCity().compareTo(o.getCity());
        } else if (this.getPostCode() != o.getPostCode()) {
            return Integer.compare(this.getPostCode(), o.getPostCode());
        } else if (!this.getStreet().equals(o.getStreet())) {
            return this.getStreet().compareTo(o.getStreet());
        } else {
            return this.getHouseNumber().compareTo(o.getHouseNumber());
        }
    }

    @Override
    public String toString() {
        return postCode + " " + municipality + ": " + city + " " + street + " " + houseNumber + " (" + closestGraphPoint
                + ")";
    }

    /**
     * A domain specific toString implementation.
     * 
     * @param use specify "standard" to get a multiple line string, or "oneLine" to
     *            get a one line string
     * @return the Address as a String
     */
    public String toString(String use) {
        if (use.equals("standard")) {
            return street + " " + houseNumber + "\n" + postCode + " " + city;
        } else if (use.equals("oneLine")) {
            return street + " " + houseNumber + ", " + postCode + " " + city;
        }
        return null;
    }
}
