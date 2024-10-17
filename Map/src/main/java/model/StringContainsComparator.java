package model;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The class StringContainsComparator implements Comparator<String> and Serializable
 * It has a Boolean strContains method to check whether one string is contained in the other
 * It returns false if the string o1 length is larger than o2, or if there is a character in o1 that is different from o2
 */


/**
 * Compares two strings based on the comparison that happens in strContains method
 * @param o1, the string that asserted to be contained in o2
 * @param o2, the string asserted to contain o1
 * @return true if o1 is in o2, false otherwise.
 */
public class StringContainsComparator implements Comparator<String>, Serializable{
    
    private static Boolean strContains(String o1, String o2) {
        if(o1.length() > o2.length()) { return false; }
        for(int i = 0; i < o1.length(); i++) {
            if(o1.charAt(i) != o2.charAt(i)) { return false;}
        }
        return true; 
    }
    /**
     * Compares two strings based on the comparison that happens in strContains method
     * @param o1 the first string in the comparison
     * @param o2 the second string in the comparison
     * @return returns 0 if string o1 is contained in o2, returns 1 otherwise
     */
    @Override
    public int compare(String o1, String o2) {
        return strContains(o1, o2) ? 0 : o1.compareTo(o2);
    }
}
