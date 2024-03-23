package types;

import java.util.List;

/**
 * Class that contains a list<list<list<list<string>>>> variable that represents the coordinates
 * of a given area
 */
public class Geometry {
    private String type;
    private List<List<List<List<Double>>>> coordinates;

    // Getters and setters

    /**
     * Method that returns the type variable
     * @return type instance
     */
    public String getType() {
        return type;
    }

    /**
     * Method that sets the type to a new value passed in.
     * @param type new type value for the current type value to be set to
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Method that gets the corrdinates of a given area
     * @return coordinate variable
     */
    public List<List<List<List<Double>>>> getCoordinates() {
        return coordinates;
    }

    /**
     * Method that sets the coordinate value to a new passed in value
     * @param coordinates new coordinate value for the current coordinate value to be set to.
     */
    public void setCoordinates(List<List<List<List<Double>>>> coordinates) {
        this.coordinates = coordinates;
    }
}