package types;

import java.util.Map;

/**
 * Class representing the properties of the GeoJson including the state, city, name , descriptions,
 * and HolcGrade.
 */
public class GeoJsonProperties {
    private String state;
    private String city;
    private String name;
    private String holc_id;
    private String holc_grade;
    private Map<String, String> area_description_data;

    /**
     * Retrieval method for the state value of the geojson
     * @return
     */
    public String getState() {
        return state;
    }

    /**
     * Method that sets the value for the state value of the geojson
     * @param state new value to have the state variable set to
     */
    public void setState(String state) {
        this.state = state;
    }

    /**
     * Method that retrieves the city value of the geojson.
     * @return the city value
     */
    public String getCity() {
        return city;
    }

    /**
     * Method that sets the value for the city value of the geojson.
     * @param city new city value for the current city value to be set to
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Method that retrieves the name of the geojson
     * @return name variable
     */
    public String getName() {
        return name;
    }

    /**
     * Method that sets the name variable to a new value that is passed in.
     * @param name new name value for the current name value to be set to
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method that retrieves the holcID of the geojson
     * @return holcID variable
     */
    public String getHolcId() {
        return holc_id;
    }

    /**
     * Method that sets the holcID variable to a new value that is passed in.
     * @param holcId new holcID value for the current holc_id value to be set to
     */
    public void setHolcId(String holcId) {
        this.holc_id = holcId;
    }

    /**
     * Method that retrieves the holc grade of the geojson
     * @return holc grade variable
     */
    public String getHolcGrade() {
        return holc_grade;
    }

    /**
     * Method that sets the holcGrade variable to a new value that is passed in.
     * @param holcGrade new holcGrade value for the current holcGrade value to be set to
     */
    public void setHolcGrade(String holcGrade) {
        this.holc_grade = holcGrade;
    }

    /**
     * Method that retrieves the area description in the geojson
     * @return map containing the descriptions for the area
     */
    public Map<String, String> getAreaDescriptionData() {
        return area_description_data;
    }

    /**
     * Method that sets the AreaDescriptionData map to a new value that is passed in
     * @param areaDescriptionData new map value for the current areaDescription value to be set to
     */
    public void setAreaDescriptionData(Map<String, String> areaDescriptionData) {
        this.area_description_data = areaDescriptionData;
    }
}