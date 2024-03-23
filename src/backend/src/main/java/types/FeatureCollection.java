package types;

import java.util.List;

/**
 * FeatureCollection class that contains setter and getter methods that are to be used in the
 * redliningHandler class to get the features and type of the data, as well as set those values.
 *
 */
public class FeatureCollection {
    private String type;
    private List<Feature> features;

    // Getters and setters

    /**
     * Method that retrieves the type variable
     * @return type instance variable
     */
    public String getType() {
        return type;
    }

    /**
     * Method that sets the value for the type
     * @param type the new value that the type value should be set to
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Retrieval method of the list of features , used in redliningHandler class
     * @return list of features
     */
    public List<Feature> getFeatures() {
        return features;
    }

    /**
     * Sets the value of the list of features to a new value
     * @param features the new list of features to be set to the current list of features
     */
    public void setFeatures(List<Feature> features) {
        this.features = features;
    }
}